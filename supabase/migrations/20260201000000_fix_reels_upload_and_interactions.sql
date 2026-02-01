-- Migration to fix Reels upload and interactions
-- Applied on 2026-02-01

-- 1. Create the reels bucket
INSERT INTO storage.buckets (id, name, public)
VALUES ('reels', 'reels', true)
ON CONFLICT (id) DO NOTHING;

-- 2. Storage policies for the 'reels' bucket
-- Drop existing to avoid conflicts
DROP POLICY IF EXISTS "Public Access" ON storage.objects;
DROP POLICY IF EXISTS "Authenticated Upload" ON storage.objects;
DROP POLICY IF EXISTS "Owner Management" ON storage.objects;

CREATE POLICY "Public Access" ON storage.objects FOR SELECT USING (bucket_id = 'reels');

CREATE POLICY "Authenticated Upload" ON storage.objects
FOR INSERT WITH CHECK (
    bucket_id = 'reels'
    AND auth.role() = 'authenticated'
    AND (storage.foldername(name))[1] = auth.uid()::text
);

CREATE POLICY "Owner Management" ON storage.objects
FOR ALL USING (
    bucket_id = 'reels'
    AND auth.role() = 'authenticated'
    AND (storage.foldername(name))[1] = auth.uid()::text
);

-- 3. Fix Database Triggers and Functions
-- Create or update log_reel_opposition function with UPSERT logic and SECURITY DEFINER
CREATE OR REPLACE FUNCTION public.log_reel_opposition()
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
        )
        ON CONFLICT (reel_id, opposer_user_id)
        DO UPDATE SET
            is_anonymous = EXCLUDED.is_anonymous,
            created_at = NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create the trigger for logging oppositions
DROP TRIGGER IF EXISTS on_reel_opposition ON public.reel_interactions;
CREATE TRIGGER on_reel_opposition
AFTER INSERT ON public.reel_interactions
FOR EACH ROW EXECUTE FUNCTION log_reel_opposition();

-- Ensure update_reel_interaction_counts is SECURITY DEFINER
CREATE OR REPLACE FUNCTION public.update_reel_interaction_counts()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        IF (NEW.interaction_type = 'like') THEN
            UPDATE public.reels SET likes_count = likes_count + 1 WHERE id = NEW.reel_id;
        ELSIF (NEW.interaction_type = 'oppose') THEN
            UPDATE public.reels SET oppose_count = oppose_count + 1 WHERE id = NEW.reel_id;
        ELSIF (NEW.interaction_type = 'save') THEN
            UPDATE public.reels SET share_count = share_count + 1 WHERE id = NEW.reel_id;
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
$function$;

-- Update bidirectional sync functions to be SECURITY DEFINER
CREATE OR REPLACE FUNCTION public.sync_reel_interactions_to_reactions()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    v_post_id text;
BEGIN
    IF pg_trigger_depth() > 1 THEN RETURN NEW; END IF;
    v_post_id := NEW.reel_id::text;

    IF (TG_OP = 'INSERT') THEN
        IF (NEW.interaction_type = 'like') THEN
            INSERT INTO public.reactions (post_id, user_id, reaction_type)
            VALUES (v_post_id, NEW.user_id, 'LIKE')
            ON CONFLICT DO NOTHING;
        END IF;
    ELSIF (TG_OP = 'DELETE') THEN
        IF (OLD.interaction_type = 'like') THEN
            DELETE FROM public.reactions
            WHERE post_id = OLD.reel_id::text AND user_id = OLD.user_id AND reaction_type = 'LIKE';
        END IF;
    END IF;
    RETURN NULL;
END;
$function$;

CREATE OR REPLACE FUNCTION public.sync_reactions_to_reel_interactions()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    IF pg_trigger_depth() > 1 THEN RETURN NEW; END IF;
    IF (NEW.reaction_type = 'LIKE' AND NEW.post_id IN (SELECT id::text FROM public.reels)) THEN
        IF (TG_OP = 'INSERT') THEN
            INSERT INTO public.reel_interactions (user_id, reel_id, interaction_type)
            VALUES (NEW.user_id, NEW.post_id::uuid, 'like')
            ON CONFLICT DO NOTHING;
        ELSIF (TG_OP = 'DELETE') THEN
            DELETE FROM public.reel_interactions
            WHERE user_id = OLD.user_id AND reel_id = OLD.post_id::uuid AND interaction_type = 'like';
        END IF;
    END IF;
    RETURN NULL;
END;
$function$;

CREATE OR REPLACE FUNCTION public.sync_reel_comments_to_post_comments()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    IF pg_trigger_depth() > 1 THEN RETURN NEW; END IF;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO public.comments (id, post_id, user_id, content, created_at, updated_at)
        VALUES (NEW.id, NEW.reel_id::text, NEW.user_id, NEW.content, NEW.created_at, NEW.updated_at);
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE public.comments SET content = NEW.content, updated_at = NEW.updated_at WHERE id = NEW.id;
    ELSIF (TG_OP = 'DELETE') THEN
        DELETE FROM public.comments WHERE id = OLD.id;
    END IF;
    RETURN NEW;
END;
$function$;

CREATE OR REPLACE FUNCTION public.sync_post_comments_to_reel_comments()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    IF pg_trigger_depth() > 1 THEN RETURN NEW; END IF;
    IF (NEW.post_id IN (SELECT id::text FROM public.reels)) THEN
        IF (TG_OP = 'INSERT') THEN
            INSERT INTO public.reel_comments (id, reel_id, user_id, content, created_at, updated_at)
            VALUES (NEW.id, NEW.post_id::uuid, NEW.user_id, NEW.content, NEW.created_at, NEW.updated_at)
            ON CONFLICT DO NOTHING;
        ELSIF (TG_OP = 'UPDATE') THEN
            UPDATE public.reel_comments SET content = NEW.content, updated_at = NEW.updated_at WHERE id = NEW.id;
        ELSIF (TG_OP = 'DELETE') THEN
            DELETE FROM public.reel_comments WHERE id = OLD.id;
        END IF;
    END IF;
    RETURN NEW;
END;
$function$;
