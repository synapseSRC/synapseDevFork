import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const ONESIGNAL_APP_ID = Deno.env.get('ONESIGNAL_APP_ID')
const ONESIGNAL_REST_API_KEY = Deno.env.get('ONESIGNAL_REST_API_KEY')

serve(async (req) => {
  try {
    if (!ONESIGNAL_APP_ID || !ONESIGNAL_REST_API_KEY) {
      throw new Error('Missing OneSignal configuration')
    }

    const body = await req.json()
    const { recipient_player_id, recipient_id, message, headings, data, sender_id, type } = body

    const payload: any = {
      app_id: ONESIGNAL_APP_ID,
      contents: { en: message || "New Notification" },
      headings: headings || { en: "Synapse" },
      data: data || {},
    }

    if (recipient_player_id) {
       payload.include_player_ids = [recipient_player_id]
    } else if (recipient_id) {
       payload.include_external_user_ids = [recipient_id]
    } else {
        return new Response(JSON.stringify({ error: "No recipient specified" }), { status: 400 })
    }

    console.log("Sending push to OneSignal:", JSON.stringify(payload))

    const response = await fetch("https://onesignal.com/api/v1/notifications", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Basic ${ONESIGNAL_REST_API_KEY}`,
      },
      body: JSON.stringify(payload),
    })

    const result = await response.json()
    console.log("OneSignal Result:", result)

    return new Response(
      JSON.stringify(result),
      { headers: { "Content-Type": "application/json" } },
    )
  } catch (error) {
    console.error(error)
    return new Response(
      JSON.stringify({ error: error.message }),
      { status: 500, headers: { "Content-Type": "application/json" } },
    )
  }
})
