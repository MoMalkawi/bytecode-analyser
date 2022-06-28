package org.rsminion.core.gamepack;

import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.rsminion.tools.deobfuscators.DeobfuscatorHandler;
import org.rsminion.tools.utils.Logger;
import org.rsminion.tools.utils.Utils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class GamePackHandler {

    private @Setter static String basePath = "/Volumes/Extreme SSD/Private Projects/RSMinionUpdater/res";

    private static @Setter boolean freshInstallation; //Set to true to force re-installation

    public static void init() {
        getBasePath();
        if(freshInstallation || !loadGamePack(GamePacks.DEOB)) {
            if((downloadGamePack() && loadGamePack(GamePacks.RAW)) || loadGamePack(GamePacks.RAW)) {
                DeobfuscatorHandler.run();
                saveGamePack(GamePacks.DEOB);
            }
        }
    }

    private static boolean loadGamePack(GamePacks type) {
        boolean rootCheck = freshInstallation && type.equals(GamePacks.RAW);
        if(!gamePackMissing(type, rootCheck)) {
            Map<String, ClassNode> classes = new HashMap<>();
            try (JarFile deobed = new JarFile(pathBuilder(type, rootCheck).toString())) {
                Enumeration<?> enumeration = deobed.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry entry = (JarEntry) enumeration.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        ClassReader classReader = new ClassReader(deobed.getInputStream(entry));
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                        classes.put(classNode.name, classNode);
                    }
                }
                Logger.info("[GamePackHandler] Loaded " + classes.size() + " Classes.");
                GamePack.setClasses(classes);
                if(type.equals(GamePacks.RAW))
                    setUpGamePackDir();
                return true;
            } catch (IOException e) {
                Logger.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    private static void setUpGamePackDir() throws IOException {
        if(GamePack.getRevision() != -1) {
            Logger.info("[GamePack] Identified GamePack Version: " + GamePack.getRevision());
            File dir = new File(basePath + File.separator + GamePack.getRevision());
            if(dir.exists()) FileUtils.deleteDirectory(dir);
            if(dir.mkdir()) {
                //Move raw
                FileUtils.moveFile(new File(basePath + File.separator + "RawGamePack.jar"),
                        new File(dir.getAbsolutePath() + File.separator + "RawGamePack.jar"));
                Logger.info("[GamePackHandler] Moved RawGamePack.jar to: " + dir.getAbsolutePath());
                basePath = dir.getAbsolutePath();
                freshInstallation = false;
            }
        } else Logger.error("[GamePackHandler] GamePack Revision is Missing.");
    }

    private static void saveGamePack(GamePacks type) {
        final File file = new File(pathBuilder(type, false).toString());
        try (final JarOutputStream out = new JarOutputStream(new FileOutputStream(file))) {
            for (final ClassNode c : GamePack.getClasses().values()) {
                out.putNextEntry(new JarEntry(c.name + ".class"));
                final ClassWriter cw = new ClassWriter(0);
                c.accept(cw);
                out.write(cw.toByteArray());
                out.closeEntry();
            }
            out.flush();
            Logger.info("[GamePackHandler] Saved Deobfuscated GamePack to ("+file.getAbsolutePath()+")");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean downloadGamePack() {
        if(gamePackMissing(GamePacks.RAW, freshInstallation)) {
            try {
                String gamePackLink = fetchGamePackLink();
                if (gamePackLink != null) {
                    URL url = new URL(gamePackLink);
                    Path targetPath = pathBuilder(GamePacks.RAW, freshInstallation);

                    Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    Logger.info("[GamePackHandler] Downloaded GamePack.");
                    return true;
                } else {
                    Logger.info("[GamePackHandler] Problem fetching GamePack Link.");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private static String fetchGamePackLink() {
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(new URL("http://oldschool3.runescape.com/k=3/jav_config.ws").
                        openStream()))) {
            StringBuilder sb = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().startsWith("codebase")) {
                    sb = new StringBuilder();
                    sb.append(line.split("=")[1]);
                } else if (line.toLowerCase().startsWith("initial_jar")) {
                    if (sb != null) sb.append(line.split("=")[1]);
                    break;
                }
            }
            if (sb != null) {
                String link = sb.toString();
                Logger.info("[GamePackHandler] Fetched GamePack URL: (" + link + ")");
                return link;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void getBasePath() {
        Scanner scanner = new Scanner(System.in);
        /* Base Path Input */
        Logger.info("[GamePackHandler] Paste your base directory here (leave empty to keep as default):");
        String path = scanner.nextLine();
        if(path != null && !path.isEmpty())
            setBasePath(path);
    }

    private static boolean gamePackMissing(GamePacks type, boolean root) {
        return !new File(pathBuilder(type, root).toString()).exists();
    }

    private static Path pathBuilder(GamePacks type, boolean root) {
        StringBuilder sb = new StringBuilder();
        String gamePackFolder = null;
        if(!root) {
            File[] files = new File(basePath).listFiles();
            if (files != null && files.length > 0) {
                int maxNumber = 0;
                for (File file : files) {
                    if (file.isDirectory() && Utils.isNumber(file.getName()))
                        maxNumber = Math.max(maxNumber, Integer.parseInt(file.getName()));
                }
                if(maxNumber > 0) gamePackFolder = String.valueOf(maxNumber);
                else {
                    freshInstallation = true;
                    return pathBuilder(type, true);
                }
            }
        }
        sb.append(basePath).append(File.separator);
        if(gamePackFolder != null) {
            sb.append(gamePackFolder).
                    append(File.separator);
        }
        sb.append(type.jarName);
        return new File(sb.toString()).toPath();
    }

    @SuppressWarnings("unused")
    public enum GamePacks {
        RAW("RawGamePack.jar"),
        DEOB("DeobGamePack.jar"),
        REFACTORED("RefactoredGamePack.jar");

        private String jarName;

        GamePacks(String jarName) {
            this.jarName = jarName;
        }

    }

}
