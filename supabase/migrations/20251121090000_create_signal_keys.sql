-- Create signal_identity_keys table
create table if not exists public.signal_identity_keys (
  user_id text references public.users(uid) on delete cascade,
  device_id integer not null,
  registration_id integer not null,
  identity_key text not null, -- Base64 encoded
  signed_pre_key_id integer not null,
  signed_pre_key text not null, -- Base64 encoded
  signed_pre_key_signature text not null, -- Base64 encoded
  created_at timestamptz default now(),
  updated_at timestamptz default now(),
  primary key (user_id, device_id)
);

-- Create signal_one_time_pre_keys table
create table if not exists public.signal_one_time_pre_keys (
  id uuid primary key default gen_random_uuid(),
  user_id text references public.users(uid) on delete cascade,
  key_id integer not null,
  public_key text not null, -- Base64 encoded
  created_at timestamptz default now(),
  unique(user_id, key_id)
);

-- Enable RLS
alter table public.signal_identity_keys enable row level security;
alter table public.signal_one_time_pre_keys enable row level security;

-- Policies for signal_identity_keys
create policy "Users can insert/update their own identity keys"
  on public.signal_identity_keys for all
  using (auth.uid()::text = user_id)
  with check (auth.uid()::text = user_id);

create policy "Authenticated users can view identity keys"
  on public.signal_identity_keys for select
  using (auth.role() = 'authenticated');

-- Policies for signal_one_time_pre_keys
create policy "Users can insert their own pre keys"
  on public.signal_one_time_pre_keys for insert
  with check (auth.uid()::text = user_id);

create policy "Users can delete their own pre keys"
  on public.signal_one_time_pre_keys for delete
  using (auth.uid()::text = user_id);

-- Note: In a real Signal implementation, fetching a PreKey should delete it (atomically).
-- Supabase HTTP API doesn't support transactional "fetch and delete" easily for public access.
-- We might need a Database Function (RPC) to claim a prekey safely.

create or replace function claim_one_time_pre_key(target_user_id text)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  pre_key_record record;
begin
  -- Select a random pre-key for the user
  select * into pre_key_record
  from public.signal_one_time_pre_keys
  where user_id = target_user_id
  order by random()
  limit 1
  for update skip locked; -- Lock it

  if pre_key_record is null then
    return null;
  end if;

  -- Delete it (consumed)
  delete from public.signal_one_time_pre_keys
  where id = pre_key_record.id;

  return to_jsonb(pre_key_record);
end;
$$;

-- Restrict access to authenticated users
revoke all on function claim_one_time_pre_key(text) from public;
grant execute on function claim_one_time_pre_key(text) to authenticated;
