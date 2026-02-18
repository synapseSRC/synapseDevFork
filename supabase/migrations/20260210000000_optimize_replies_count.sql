CREATE OR REPLACE FUNCTION increment_replies_count(comment_id UUID, delta INT)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE comments
  SET replies_count = replies_count + delta
  WHERE id = comment_id;
END;
$$;
