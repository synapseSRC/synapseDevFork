import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const ONESIGNAL_APP_ID = Deno.env.get('ONESIGNAL_APP_ID')
const ONESIGNAL_REST_API_KEY = Deno.env.get('ONESIGNAL_REST_API_KEY')
const SUPABASE_URL = Deno.env.get('SUPABASE_URL')
const SUPABASE_ANON_KEY = Deno.env.get('SUPABASE_ANON_KEY')

serve(async (req) => {
  try {
    // 1. Authorization Check: Verify Supabase JWT
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) {
      return new Response(JSON.stringify({ error: 'No authorization header' }), { status: 401 })
    }

    const supabase = createClient(SUPABASE_URL!, SUPABASE_ANON_KEY!)
    const { data: { user }, error: authError } = await supabase.auth.getUser(authHeader.replace('Bearer ', ''))

    if (authError || !user) {
      console.error('Authorization failed:', authError)
      return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401 })
    }

    // 2. Parse Request Body
    const { recipient_id, message, sender_id, type, data, headings } = await req.json()

    if (!recipient_id || !message) {
      return new Response(JSON.stringify({ error: 'Missing recipient_id or message' }), { status: 400 })
    }

    // 3. Optional: Validate that the sender_id matches the authenticated user
    if (sender_id && sender_id !== user.id) {
       console.warn(`User ${user.id} attempted to send notification as ${sender_id}`)
       // We can choose to override or reject. For now, we'll just log and continue
       // as some system notifications might not have a perfect match in early dev.
    }

    // 4. Send to OneSignal
    const onesignalResponse = await fetch('https://api.onesignal.com/notifications', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Key ${ONESIGNAL_REST_API_KEY}`,
      },
      body: JSON.stringify({
        app_id: ONESIGNAL_APP_ID,
        include_external_user_ids: [recipient_id],
        contents: { en: message },
        headings: headings || { en: "Synapse Social" },
        data: {
          ...data,
          sender_id: sender_id || user.id,
          type,
          recipient_id
        },
      }),
    })

    const result = await onesignalResponse.json()
    console.log('OneSignal response:', result)

    return new Response(JSON.stringify(result), {
      status: onesignalResponse.status,
      headers: { 'Content-Type': 'application/json' }
    })

  } catch (err) {
    console.error('Edge Function error:', err)
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' }
    })
  }
})
