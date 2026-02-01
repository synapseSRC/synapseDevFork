-- Create reels table
CREATE TABLE IF NOT EXISTS public.reels (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    creator_id TEXT NOT NULL REFERENCES public.users(uid),
    video_url TEXT NOT NULL,
    thumbnail_url TEXT,
    caption TEXT,
    music_track TEXT,
    likes_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    share_count INTEGER DEFAULT 0,
    oppose_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create reel_interactions table
CREATE TABLE IF NOT EXISTS public.reel_interactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.users(uid),
    reel_id UUID NOT NULL REFERENCES public.reels(id) ON DELETE CASCADE,
    interaction_type TEXT NOT NULL CHECK (interaction_type IN ('like', 'oppose', 'save')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    anonymous_oppose BOOLEAN DEFAULT FALSE,
    UNIQUE(user_id, reel_id, interaction_type)
);

-- Create reel_opposers_log table
CREATE TABLE IF NOT EXISTS public.reel_opposers_log (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    reel_id UUID NOT NULL REFERENCES public.reels(id) ON DELETE CASCADE,
    opposer_user_id TEXT NOT NULL REFERENCES public.users(uid),
    creator_id TEXT NOT NULL REFERENCES public.users(uid),
    is_anonymous BOOLEAN DEFAULT TRUE,
    conversation_started BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(reel_id, opposer_user_id)
);

-- Create reel_comments table
CREATE TABLE IF NOT EXISTS public.reel_comments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    reel_id UUID NOT NULL REFERENCES public.reels(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL REFERENCES public.users(uid),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Update user_settings table
ALTER TABLE public.user_settings ADD COLUMN IF NOT EXISTS enable_capture_opposer BOOLEAN DEFAULT FALSE;

-- Enable RLS
ALTER TABLE public.reels ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reel_interactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reel_opposers_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reel_comments ENABLE ROW LEVEL SECURITY;

-- Policies for reels
CREATE POLICY "Public reels are viewable by everyone" ON public.reels FOR SELECT USING (true);
CREATE POLICY "Users can insert their own reels" ON public.reels FOR INSERT WITH CHECK (creator_id = auth.uid()::text);
CREATE POLICY "Users can update their own reels" ON public.reels FOR UPDATE USING (creator_id = auth.uid()::text);
CREATE POLICY "Users can delete their own reels" ON public.reels FOR DELETE USING (creator_id = auth.uid()::text);

-- Policies for reel_interactions
CREATE POLICY "Interactions are viewable by everyone" ON public.reel_interactions FOR SELECT USING (true);
CREATE POLICY "Users can insert their own interactions" ON public.reel_interactions FOR INSERT WITH CHECK (user_id = auth.uid()::text);
CREATE POLICY "Users can update their own interactions" ON public.reel_interactions FOR UPDATE USING (user_id = auth.uid()::text);
CREATE POLICY "Users can delete their own interactions" ON public.reel_interactions FOR DELETE USING (user_id = auth.uid()::text);

-- Policies for reel_opposers_log
CREATE POLICY "Creators can view their reels opposers" ON public.reel_opposers_log FOR SELECT USING (creator_id = auth.uid()::text);
CREATE POLICY "Users can view their own opposition history" ON public.reel_opposers_log FOR SELECT USING (opposer_user_id = auth.uid()::text);
CREATE POLICY "Users can insert opposition log" ON public.reel_opposers_log FOR INSERT WITH CHECK (opposer_user_id = auth.uid()::text);

-- Policies for reel_comments
CREATE POLICY "Comments are viewable by everyone" ON public.reel_comments FOR SELECT USING (true);
CREATE POLICY "Users can insert their own comments" ON public.reel_comments FOR INSERT WITH CHECK (user_id = auth.uid()::text);
CREATE POLICY "Users can update their own comments" ON public.reel_comments FOR UPDATE USING (user_id = auth.uid()::text);
CREATE POLICY "Users can delete their own comments" ON public.reel_comments FOR DELETE USING (user_id = auth.uid()::text);
