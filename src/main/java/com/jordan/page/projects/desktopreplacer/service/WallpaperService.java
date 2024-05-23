package com.jordan.page.projects.desktopreplacer.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import com.sun.jna.platform.win32.Advapi32Util;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinReg;

import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WallpaperService {

    private static final int SPI_SETDESKWALLPAPER = 0x0014;
    private static final int SPIF_UPDATEINIFILE = 0x01;
    private static final int SPIF_SENDCHANGE = 0x02;

    public interface User32Extended extends StdCallLibrary {
        User32Extended INSTANCE = Native.load("user32", User32Extended.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean EnumDisplayDevices(String lpDevice, int iDevNum, DISPLAY_DEVICE lpDisplayDevice, int dwFlags);

        boolean EnumDisplaySettings(char[] lpszDeviceName, int iModeNum, DEVMODE lpDevMode);

        boolean SystemParametersInfo(int uiAction, int uiParam, String pvParam, int fWinIni);
    }

    @Structure.FieldOrder({
            "cb", "DeviceName", "DeviceString", "StateFlags", "DeviceID", "DeviceKey"
    })
    public static class DISPLAY_DEVICE extends Structure {
        public WinDef.DWORD cb;
        public char[] DeviceName = new char[32];
        public char[] DeviceString = new char[128];
        public WinDef.DWORD StateFlags;
        public char[] DeviceID = new char[128];
        public char[] DeviceKey = new char[128];

        public DISPLAY_DEVICE() {
            cb = new WinDef.DWORD(size());
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("cb", "DeviceName", "DeviceString", "StateFlags", "DeviceID", "DeviceKey");
        }
    }

    @Structure.FieldOrder({
            "dmDeviceName", "dmSpecVersion", "dmDriverVersion", "dmSize", "dmDriverExtra",
            "dmFields", "dmPositionX", "dmPositionY", "dmDisplayOrientation", "dmDisplayFixedOutput",
            "dmColor", "dmDuplex", "dmYResolution", "dmTTOption", "dmCollate", "dmFormName",
            "dmLogPixels", "dmBitsPerPel", "dmPelsWidth", "dmPelsHeight", "dmDisplayFlags",
            "dmDisplayFrequency", "dmICMMethod", "dmICMIntent", "dmMediaType", "dmDitherType",
            "dmReserved1", "dmReserved2", "dmPanningWidth", "dmPanningHeight"
    })
    public static class DEVMODE extends Structure {
        public char[] dmDeviceName = new char[32];
        public WinDef.WORD dmSpecVersion;
        public WinDef.WORD dmDriverVersion;
        public WinDef.WORD dmSize;
        public WinDef.WORD dmDriverExtra;
        public WinDef.DWORD dmFields;
        public WinDef.LONG dmPositionX;
        public WinDef.LONG dmPositionY;
        public WinDef.DWORD dmDisplayOrientation;
        public WinDef.DWORD dmDisplayFixedOutput;
        public WinDef.WORD dmColor;
        public WinDef.WORD dmDuplex;
        public WinDef.WORD dmYResolution;
        public WinDef.WORD dmTTOption;
        public WinDef.WORD dmCollate;
        public char[] dmFormName = new char[32];
        public WinDef.WORD dmLogPixels;
        public WinDef.DWORD dmBitsPerPel;
        public WinDef.DWORD dmPelsWidth;
        public WinDef.DWORD dmPelsHeight;
        public WinDef.DWORD dmDisplayFlags;
        public WinDef.DWORD dmDisplayFrequency;
        public WinDef.DWORD dmICMMethod;
        public WinDef.DWORD dmICMIntent;
        public WinDef.DWORD dmMediaType;
        public WinDef.DWORD dmDitherType;
        public WinDef.DWORD dmReserved1;
        public WinDef.DWORD dmReserved2;
        public WinDef.DWORD dmPanningWidth;
        public WinDef.DWORD dmPanningHeight;

        public DEVMODE() {
            dmSize = new WinDef.WORD(size());
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "dmDeviceName", "dmSpecVersion", "dmDriverVersion", "dmSize", "dmDriverExtra", "dmFields",
                    "dmPositionX", "dmPositionY", "dmDisplayOrientation", "dmDisplayFixedOutput", "dmColor", "dmDuplex",
                    "dmYResolution", "dmTTOption", "dmCollate", "dmFormName", "dmLogPixels", "dmBitsPerPel",
                    "dmPelsWidth",
                    "dmPelsHeight", "dmDisplayFlags", "dmDisplayFrequency", "dmICMMethod", "dmICMIntent", "dmMediaType",
                    "dmDitherType", "dmReserved1", "dmReserved2", "dmPanningWidth", "dmPanningHeight");
        }
    }

    @Value("${baseUrl}")
    private String url;

    @Value("${baseDir}")
    private String baseDir;

    private final Random random;

    @Autowired
    public WallpaperService(Random random) {
        this.random = random;
    }

    public interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SystemParametersInfo(int uiAction, int uiParam, String pvParam, int fWinIni);
    }

    public void changeWallpaper(String search) throws IOException, InterruptedException {
        enumerateMonitors();
        String src = findImageUrl(search);
        String file = downloadImage(src);
        clearTranscodedImageCache();
        setDesktopWallpaper(file, 0);
        refreshDesktop();

    }

    private String findImageUrl(String search) throws IOException, InterruptedException {
        String[] searchSpl = search.split(" ");
        log.info("Search Split: {}", Arrays.toString(searchSpl));

        String fullUrl = url;
        for (int i = 0; i < searchSpl.length; i++) {

            if (i == searchSpl.length || i == 0) {
                fullUrl = fullUrl.concat(searchSpl[i]);
            } else {
                fullUrl = fullUrl.concat("+").concat(searchSpl[i]);
            }

            log.info(fullUrl);
        }

        log.info("FULL URL: {}", fullUrl);
        String userAgent = "Mozilla/5.0";
        Document doc = Jsoup.connect(fullUrl).userAgent(userAgent).get();
        Elements divs = doc.select("div.qodef-e-inner");

        String src = "";
        if (!divs.isEmpty()) {
            while (true) {
                int randomIndex = random.nextInt(divs.size());
                Element randomDiv = divs.get(randomIndex);
                Element innerDiv = randomDiv.selectFirst("div.qodef-e-image");
                Element aTag = innerDiv.selectFirst("a");
                String href = aTag.attr("href");
                Document doc2 = Jsoup.connect(href).userAgent(userAgent).get();
                Elements imgs = doc2.select("img.size-full.attachment-full");
                Element wallpaper = null;

                for (Element img : imgs) {

                    if (img.hasAttr("fetchpriority")) {
                        wallpaper = img;
                        break;
                    }
                }

                if (wallpaper == null) {
                    log.error("Wallpaper couln't be found");
                    System.exit(0);
                }

                src = wallpaper.attr("data-lazy-src");

                if (!src.contains("data:image")) {
                    log.info("\n***** Valid Random Image: {}", src);
                    break;
                } else {
                    log.info("\n***** Invalid Random Image: {}", src);
                }
            }

        } else {
            log.error("\n***** Error grabbing images. Exiting....");
            throw new IllegalArgumentException();
        }
        return src;
    }

    private String downloadImage(String src) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(src))
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 200) {
                String fullPath = constructDate(src);
                try (InputStream in = new BufferedInputStream(response.body());
                        FileOutputStream out = new FileOutputStream(new File(fullPath))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                return fullPath;
            } else {
                log.error("\n***** Error: Failed to download image. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("\n***** Exception: ", e);
        }
        return null;
    }

    private void setDesktopWallpaper(String imagePath, int displayIndex) {
        log.info("\n Set Desktop Wallpaper!");
        String wallpaperKey = "Control Panel\\Desktop";
        String wallpaperValue = "Wallpaper" + (displayIndex == 0 ? "" : displayIndex);
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, wallpaperKey, wallpaperValue, imagePath);
    }

    private static void refreshDesktop() {
        log.info("\n Refresh Desktop!");
        User32.INSTANCE.SystemParametersInfo(SPI_SETDESKWALLPAPER, 0, null, SPIF_UPDATEINIFILE | SPIF_SENDCHANGE);
    }

    private static void clearTranscodedImageCache() {
        log.info("\n Clear Transcoded Image Cache");
        String transcodedImageCacheKey = "Control Panel\\Desktop";

        // Clear the TranscodedImageCache
        Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, transcodedImageCacheKey, "TranscodedImageCache");

        // Clear additional cached images if present
        for (int i = 0; i < 10; i++) {
            String valueName = "TranscodedImageCache_" + String.format("%d", i);
            if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, transcodedImageCacheKey, valueName)) {
                Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, transcodedImageCacheKey, valueName);
            }
        }
    }

    private String constructDate(String src) {
        String filename = "";
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int lastSlashIdx = src.lastIndexOf('/');

        if (lastSlashIdx != -1) {
            filename = src.substring(lastSlashIdx + 1);
        }
        return baseDir + File.separator + (month + 1) + File.separator + day + File.separator + filename;
    }

    public static void enumerateMonitors() {
        User32Extended user32 = User32Extended.INSTANCE;
        DISPLAY_DEVICE displayDevice = new DISPLAY_DEVICE();

        int deviceIndex = 0;
        while (user32.EnumDisplayDevices(null, deviceIndex, displayDevice, 0)) {
            log.info("Monitor " + deviceIndex + ": " + Native.toString(displayDevice.DeviceName) + " ("
                    + Native.toString(displayDevice.DeviceString) + ")");

            DEVMODE devMode = new DEVMODE();
            if (user32.EnumDisplaySettings(displayDevice.DeviceName, -1, devMode)) {
                log.info("  Resolution: " + devMode.dmPelsWidth + "x" + devMode.dmPelsHeight);
            }

            deviceIndex++;
            displayDevice = new DISPLAY_DEVICE(); // Create a new instance for the next monitor
        }
    }

}