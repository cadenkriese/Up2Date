package com.gamerking195.dev.up2date.util;

import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.Up2Date;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Caden Kriese (GamerKing195) on 9/1/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 */
public class UtilSiteSearch {
    private UtilSiteSearch() {}
    private static UtilSiteSearch instance = new UtilSiteSearch();
    public static UtilSiteSearch getInstance() {
        return instance;
    }
    private Gson gson = new Gson();

    public ArrayList<SearchResult> searchResources(String query, int size) {
        ArrayList<SearchResult> pluginIds = new ArrayList<>();

        try {
            Type type = new TypeToken<ArrayList<JsonObject>>(){}.getType();
            String info = UtilReader.readFrom("https://api.spiget.org/v2/search/resources/"+query+"?field=name&size="+size+"&fields=name%2Ctag%2Cid%2CtestedVersions");
            ArrayList<JsonObject> objects = gson.fromJson(info, type);
            for (JsonObject object : objects) {
                ArrayList<String> testedVersions = new ArrayList<>();
                object.getAsJsonArray("testedVersions").forEach(testedVersion -> testedVersions.add(testedVersion.getAsString()));
                String[] testedVersionsArray = testedVersions.toArray(new String[0]);
                SearchResult result = new SearchResult(object.get("id").getAsInt(), object.get("name").getAsString(), object.get("tag").getAsString(), info.contains("\"premium\": true") && info.contains("\"id\": "+object.get("id").getAsInt()), testedVersionsArray);

                if (result.getName() != null && result.getTag() != null && result.getId() != 0)
                    pluginIds.add(result);
            }

        } catch (IOException ex) {
            if (ex instanceof FileNotFoundException) {
                pluginIds.clear();
                return pluginIds;
            }

            Up2Date.getInstance().printError(ex, "Error occurred while retrieving search results from spiget.");
        }

        return pluginIds;
    }

    @Getter
    @Setter
    public static class SearchResult {
        private int id;
        private String name;
        private String tag;
        private boolean premium;
        private String[] testedVersions;

        public SearchResult(int id, String name, String tag, boolean premium, String[] testedVersions) {
            this.id = id;
            this.name = name;
            this.tag = tag;
            this.premium = premium;
            this.testedVersions = testedVersions;
        }
    }
}
