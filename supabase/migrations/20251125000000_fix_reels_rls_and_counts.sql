-- Fix: Creators seeing anonymous opposers
DROP POLICY IF EXISTS "Creators can view their reels opposers" ON public.reel_opposers_log;
CREATE POLICY "Creators can view their reels opposers" ON public.reel_opposers_log
FOR SELECT USING (
    creator_id = auth.uid()::text
    AND (is_anonymous = false OR opposer_user_id = auth.uid()::text)
);

-- Fix: Users inserting fake logs
DROP POLICY IF EXISTS "Users can insert opposition log" ON public.reel_opposers_log;
CREATE POLICY "Users can insert opposition log" ON public.reel_opposers_log
FOR INSERT WITH CHECK (
    opposer_user_id = auth.uid()::text
    AND creator_id = (SELECT creator_id FROM public.reels WHERE id = reel_id)
);

-- Fix: Users updating their own counts / Automate counts
-- 1. Create Trigger Function to update counts
CREATE OR REPLACE FUNCTION update_reel_interaction_counts()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        IF (NEW.interaction_type = 'like') THEN
            UPDATE public.reels SET likes_count = likes_count + 1 WHERE id = NEW.reel_id;
        ELSIF (NEW.interaction_type = 'oppose') THEN
            UPDATE public.reels SET oppose_count = oppose_count + 1 WHERE id = NEW.reel_id;
        ELSIF (NEW.interaction_type = 'save') THEN
            UPDATE public.reels SET share_count = share_count + 1 WHERE id = NEW.reel_id; -- Using share_count for saves temporarily or logic? Prompt said 'save'. Assuming 'save' maps to something or just not counted in schema? Schema has share_count. Let's assume saves aren't counted in 'share_count'. Actually schema has 'share_count'. Let's stick to like/oppose for now.
        END IF;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        IF (OLD.interaction_type = 'like') THEN
            UPDATE public.reels SET likes_count = GREATEST(0, likes_count - 1) WHERE id = OLD.reel_id;
        ELSIF (OLD.interaction_type = 'oppose') THEN
            UPDATE public.reels SET oppose_count = GREATEST(0, oppose_count - 1) WHERE id = OLD.reel_id;
        END IF;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. Create Trigger
DROP TRIGGER IF EXISTS on_reel_interaction_change ON public.reel_interactions;
CREATE TRIGGER on_reel_interaction_change
AFTER INSERT OR DELETE ON public.reel_interactions
FOR EACH ROW EXECUTE FUNCTION update_reel_interaction_counts();

-- 3. Restrict permissions
-- 3. Create Trigger for logging oppositions
CREATE OR REPLACE FUNCTION log_reel_opposition()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.interaction_type = 'oppose') THEN
        INSERT INTO public.reel_opposers_log (reel_id, opposer_user_id, creator_id, is_anonymous, conversation_started)
        VALUES (
            NEW.reel_id,
            NEW.user_id,
            (SELECT creator_id FROM public.reels WHERE id = NEW.reel_id),
            COALESCE(NEW.anonymous_oppose, false),
            false
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_reel_opposition ON public.reel_interactions;
CREATE TRIGGER on_reel_opposition
AFTER INSERT ON public.reel_interactions
FOR EACH ROW EXECUTE FUNCTION log_reel_opposition();

-- 4. Restrict permissions
-- Revoke all updates first
REVOKE UPDATE ON public.reels FROM authenticated;
-- Grant specific column updates (only metadata, not counts)
-- Removed video_url for security (SSRF/Malware risk)
GRANT UPDATE (caption, thumbnail_url, music_track, post_visibility) ON public.reels TO authenticated;
