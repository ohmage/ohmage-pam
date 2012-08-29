
package org.openmhealth.pam;

import android.content.Context;
import android.location.Location;
import android.os.RemoteException;
import android.text.format.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.probemanager.ProbeWriter;
import org.ohmage.probemanager.ResponseBuilder;

import java.util.Date;
import java.util.UUID;

public class PamProbeWriter extends ProbeWriter {

    private static final String CAMPAIGN_URN = "urn:campaign:org:openmhealth:pam";
    private static final String CAMPAIGN_CREATED = "2012-08-28 17:48:36";

    public PamProbeWriter(Context context) {
        super(context);
    }

    public void writeResponse(Location location, JSONObject photoId) {

        JSONArray responses = new JSONArray();
        JSONObject prompt = new JSONObject();
        JSONArray photo = new JSONArray();
        try {
            photo.put(photoId);
            prompt.put("value", photo);
            prompt.put("prompt_id", "photo_id");
            responses.put(prompt);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Create the response builder for the correct campaign
        ResponseBuilder response = new ResponseBuilder(CAMPAIGN_URN, CAMPAIGN_CREATED);

        // Set all the data for the response
        response.withTime(new Date().getTime(), Time.getCurrentTimezone())
                .withSurveyKey(UUID.randomUUID().toString()).withSurveyId("pamSurvey")
                .withSurveyLaunchContext(new Date().getTime(), Time.getCurrentTimezone())
                .withResponses(responses.toString());

        if (location != null)
            response.withLocation(location, Time.getCurrentTimezone(), "valid");

        // Write the response
        try {
            response.write(this);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
