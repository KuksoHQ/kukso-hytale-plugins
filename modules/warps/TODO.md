Build successful. Here's the solution:

PlayerCmdWrapper - A new wrapper that extends AbstractPlayerCommand and adapts CmdInterface for standalone player commands.

How it works:
1. Commands implement CmdInterface from kukso-hy-lib (centralized interface)
2. PlayerCmdWrapper wraps them to work as standalone player commands
3. Registration: new PlayerCmdWrapper(new DelWarpCommand(warpManager))

To centralize in kukso-hy-lib:                                                                                                                                            
Move PlayerCmdWrapper to kukso-hy-lib so all your plugins can use it:

kukso-hy-lib/src/main/java/com/kukso/hy/lib/command/PlayerCmdWrapper.java

Then in any plugin:                                                                                                                                                       
import com.kukso.hy.lib.command.PlayerCmdWrapper;

// Register standalone player command using CmdInterface                                                                                                                  
registry.registerCommand(new PlayerCmdWrapper(new MyCommand()));

This gives you:
- Centralized interface (CmdInterface) for command logic
- Standalone commands (not sub-commands) like /delwarp
- Reusable wrapper across all your plugins    

The core issue: Hytale's PlayerRef doesn't implement CommandSender, so CmdInterface (which uses CommandSender) can't work directly with player commands.

For kukso-hy-lib: You should create a PlayerCmdInterface specifically for player commands:

public interface PlayerCmdInterface {                                                                                                                                     
String getName();                                                                                                                                                     
String getDescription();                                                                                                                                              
List<String> getAliases();                                                                                                                                            
List<String> getPermissions();                                                                                                                                        
List<String> tabComplete(PlayerRef player, String[] args);                                                                                                            
boolean execute(PlayerRef player, String[] args);                                                                                                                     
}

Then PlayerCmdWrapper can properly bridge PlayerCmdInterface to AbstractPlayerCommand.
