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
import com.sun.jna.win32.W32APIOptions;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WallpaperService {

    public static interface User32 extends Library {
        User32 INSTANCE = Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SystemParametersInfo(int one, int two, String s, int three);
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

    public void changeWallpaper(String search) throws IOException, InterruptedException {
        String src = findImageUrl(search);
        String file = downloadImage(src);
        setDesktopWallpaper(file);
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

    private void setDesktopWallpaper(String src) {
        User32.INSTANCE.SystemParametersInfo(0x0014, 0, src, 1);
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
}