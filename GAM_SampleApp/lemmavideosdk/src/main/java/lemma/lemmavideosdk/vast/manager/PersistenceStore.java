package lemma.lemmavideosdk.vast.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.VastBuilder.LinearAd;
import lemma.lemmavideosdk.vast.VastBuilder.NonLinearAd;
import lemma.lemmavideosdk.vast.VastBuilder.Vast;

import static android.content.Context.MODE_PRIVATE;

public class PersistenceStore {

    SharedPreferences mPrefs;
    Context mContext;

    public PersistenceStore(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences("vast", MODE_PRIVATE);
    }

    public void save(Vast vast) {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String json = new Gson().toJson(vast);
        prefsEditor.putString("lm", json);
        prefsEditor.commit();
    }

    public Vast retrieve() {
        Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(AdI.class, new InterfaceAdapter<AdI>())
                .create();
        String json = mPrefs.getString("lm", "");
        Vast vast = gson.fromJson(json, Vast.class);
        return vast;
    }

    final class InterfaceAdapter<T> implements JsonDeserializer<T> {

        public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {

            JsonObject obj = elem.getAsJsonObject();
            String type = obj.getAsJsonPrimitive("type").getAsString();
            if (type.equalsIgnoreCase("LinearAd")) {
                return (T) new Gson().fromJson(obj, LinearAd.class);
            } else if (type.equalsIgnoreCase("NonLinearAd")) {
                return (T) new Gson().fromJson(obj, NonLinearAd.class);
            } else {
                return context.deserialize(obj, interfaceType);
            }
        }
    }
}
