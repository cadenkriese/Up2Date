package com.gamerking195.dev.up2date.config;

import com.gamerking195.dev.up2date.Up2Date;
import lombok.Getter;
import lombok.Setter;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Path;
import net.cubespace.Yamler.Config.YamlConfig;

import java.io.File;

/**
 * Created by GamerKing195 on 8/13/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 */

@Getter
@Setter
public class MainConfig extends YamlConfig {
    public MainConfig(Up2Date plugin) {
        CONFIG_HEADER = new String[]
                                {
                                        "#################################",
                                        "                                #",
                                        "Up2Date V" + Up2Date.getInstance().getDescription().getVersion() + ", by " + Up2Date.getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", "") + " #",
                                        "                                #",
                                        "#################################",
                                        "",
                                        "Config Guide:",
                                        "",
                                        "STRING, Any text you want.",
                                        "INT, A number without a decimal.",
                                        "FLOAT, A number with a decimal.",
                                        "BOOLEAN, A string that either equals true or false.",
                                        "ARRAY, A list of values like those stated above."
                                };
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
    }


    @Comment("---------------General---------------")

    @Comments
            ({
                     "",
                     "SETUP_COMPLETE",
                     "Desc: Has the initial setup wizard been completed, set to false if you want to re-do the plugin setup.",
                     "Type: boolean",
                     "Default: false (Set to true once in-game setup is complete)"
            })
    @Path("General.Setup-Complete")
    private boolean setupComplete = false;

    @Comments
            ({
                     "",
                     "CACHE_REFRESH_DELAY",
                     "Desc: Interval for how long before we refresh the cache (check for updates) for all of your plugins.",
                     "Note: The refresh process takes an estimated your amount of plugins x 2 seconds.",
                     "Type: integer (minutes)",
                     "Minimum: 5",
                     "Default: 120"
            })
    @Path("General.Cache-Refresh-Delay")
    private int cacheRefreshDelay = 120;

    @Comments
            ({
                     "",
                     "DATABASE_REFRESH_DELAY",
                     "Desc: How often the server will ping your database and update plugin info.",
                     "Note: Only applies to people with an SQL database enabled.",
                     "Type: integer (minutes)",
                     "Minimum: 5",
                     "Default: 30"
            })
    @Path("General.Database-Refresh-Delay")
    private int databaseRefreshDelay = 30;

    @Comment("---------------Messages---------------")
    @Comments
            ({
                     "",
                     "PREFIX",
                     "Desc: Prefix to be used in the prefix variable, '%prefix%'",
                     "Type: String",
                     "Default: &d&lU&5&l2&d&lD &8&l» "
            })
    @Path("Messages.Prefix")
    private String prefix = "&5&lU&d&l2&5&lD &8&l» ";

    @Comments
            ({
                     "",
                     "NO_PERMISSION",
                     "Desc: Message sent when an user tries to do something they can't.",
                     "Type: String",
                     "Default: &dYou don't have permission to do that!"
            })
    @Path("Messages.No-Permission")
    private String noPermissionMessage = "%prefix%&dYou don't have permission to do that!";

    @Comment("---------------Advanced---------------")

    @Comments
            ({
                     "",
                     "THREAD_POOL_SIZE",
                     "Desc: Amount of threads used while parsing your plugins.",
                     "If you have 10-29 plugins leave it, 30-69 set it to around 10.",
                     "Max: 12",
                     "Type: Int",
                     "Default: 5"
            })
    @Path("Advanced.ThreadPoolSize")
    private int threadPoolSize = 5;

    @Comments
            ({
                     "",
                     "CONNECTION_POOL_SIZE",
                     "Desc: Amount of connections used while transferring data to your database.",
                     "Never set this above 10 (unless you know what you're doing and you have godly servers).",
                     "Max: 15",
                     "Type: Int",
                     "Default: 5"
            })
    @Path("Advanced.ConnectionPoolSize")
    private int connectionPoolSize = 5;

    @Comment("---------------Statistics---------------")

    @Comments
            ({
                    "",
                    "ENABLED",
                    "Desc: Do you want Up2Date to track usage data & incompatibilities to help improve it in the future?",
                    "Type: boolean",
                    "Default: true"
            })
    @Path("Statistics.Enabled")
    private boolean enableStatistics = true;

    @Comments
            ({
                    "",
                    "SERVER_ID",
                    "Desc: The unique ID for your server tracked by U2D.",
                    "Note: DO NOT CHANGE THIS!",
                    "Type: Int"
            })
    @Path("Statistics.ServerID")
    private int serverId = 0;

    @Comment("---------------SQL Database---------------")

    @Comments
            ({
                     "",
                     "ENABLE_SQL",
                     "Desc: Should Up2Date data be stored in a MySQL database so you can sync linked plugins between servers.",
                     "Type: String",
                     "Default: false"
            })
    @Path("SQL.Enable-Sql")
    private boolean enableSQL = false;

    @Comments
            ({
                     "",
                     "HOSTNAME",
                     "Desc: Hostname / IP to the MySQL DB, port included.",
                     "Type: String"
            })
    @Path("SQL.Hostname")
    private String hostName = "0.0.0.0:0000";

    @Comments
            ({
                     "",
                     "USERNAME",
                     "Desc: Username Up2Date will use to connect to the database.",
                     "Type: String"
            })
    @Path("SQL.Username")
    private String username = "root";

    @Comments
            ({
                     "",
                     "PASSWORD",
                     "Desc: Password Up2Date will use to connect to the database.",
                     "Type: String"
            })
    @Path("SQL.Password")
    private String password = "1234";

    @Comments
            ({
                     "",
                     "DATABASE",
                     "Desc: The database that Up2Date will use to store its table.",
                     "Type: String"
            })
    @Path("SQL.Database")
    private String database = "db";

    @Comments
            ({
                     "",
                     "TABLENAME",
                     "Desc: The name of the table Up2Date will use to store data.",
                     "Type: String"
            })
    @Path("SQL.Tablename")
    private String tablename = "u2d";
}
