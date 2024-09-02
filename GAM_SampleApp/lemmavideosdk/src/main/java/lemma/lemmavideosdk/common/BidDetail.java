package lemma.lemmavideosdk.common;

import org.json.JSONObject;

public class BidDetail implements DisplayableAdI {
    static final String KEY_IMPRESSION_ID = "impid";

    public String creative;
    public String impressionId;

    public BidDetail(JSONObject bidJsonObject){
        creative = bidJsonObject.optString("adm");
        impressionId = bidJsonObject.optString(KEY_IMPRESSION_ID);
    }

    @Override
    public Type getType() {
        return Type.Html;
    }
}
