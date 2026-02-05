-- Ensure unread_notifications_count exists on users
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS unread_notifications_count INTEGER DEFAULT 0;

-- Notifications table (ensuring schema matches requirements)
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    recipient_id TEXT NOT NULL REFERENCES public.users(uid) ON DELETE CASCADE,
    sender_id TEXT REFERENCES public.users(uid) ON DELETE SET NULL,
    type TEXT NOT NULL,
    data JSONB DEFAULT '{}'::jsonb,
    title JSONB,
    body JSONB,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    read_at TIMESTAMP WITH TIME ZONE,
    action_url TEXT,
    priority INTEGER DEFAULT 2,
    delivery_status notification_delivery_status DEFAULT 'pending',
    interacted_at TIMESTAMP WITH TIME ZONE
);

-- Index for performance
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_created ON public.notifications(recipient_id, created_at DESC);

-- Trigger to increment unread count
CREATE OR REPLACE FUNCTION public.handle_new_notification()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE public.users
    SET unread_notifications_count = COALESCE(unread_notifications_count, 0) + 1
    WHERE uid = NEW.recipient_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_notification_created ON public.notifications;
CREATE TRIGGER on_notification_created
AFTER INSERT ON public.notifications
FOR EACH ROW
EXECUTE FUNCTION public.handle_new_notification();

-- Trigger to decrement unread count when read
CREATE OR REPLACE FUNCTION public.handle_notification_read()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.is_read = FALSE AND NEW.is_read = TRUE THEN
        UPDATE public.users
        SET unread_notifications_count = GREATEST(COALESCE(unread_notifications_count, 0) - 1, 0)
        WHERE uid = NEW.recipient_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_notification_read ON public.notifications;
CREATE TRIGGER on_notification_read
AFTER UPDATE OF is_read ON public.notifications
FOR EACH ROW
WHEN (OLD.is_read = FALSE AND NEW.is_read = TRUE)
EXECUTE FUNCTION public.handle_notification_read();
