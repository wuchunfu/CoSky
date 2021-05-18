package me.ahoo.govern.rest.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.govern.config.Config;
import me.ahoo.govern.config.ConfigHistory;
import me.ahoo.govern.config.ConfigService;
import me.ahoo.govern.config.ConfigVersion;
import me.ahoo.govern.rest.dto.ImportResponse;
import me.ahoo.govern.rest.support.RequestPathPrefix;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author ahoo wang
 */
@CrossOrigin("*")
@RestController
@RequestMapping(RequestPathPrefix.CONFIGS_PREFIX)
@Slf4j
public class ConfigController {

    public static final String IMPORT_SUPPORT_EXT = "zip";
    public static final String IMPORT_POLICY_SKIP = "skip";
    public static final String IMPORT_POLICY_OVERWRITE = "overwrite";
    public static final String NACOS_DEFAULT_GROUP = "DEFAULT_GROUP/";
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public CompletableFuture<Set<String>> getConfigs(@PathVariable String namespace) {
        return configService.getConfigs(namespace);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ImportResponse> importZip(@PathVariable String namespace, @RequestParam String policy, @RequestPart MultipartFile importZip) throws IOException {
        var importResponse = new ImportResponse();
        if (Objects.isNull(importZip) || importZip.isEmpty()) {
            return CompletableFuture.completedFuture(importResponse);
        }
        var importFilename = importZip.getOriginalFilename();
        var importFileExt = Files.getFileExtension(importFilename).toLowerCase();
        Preconditions.checkArgument(IMPORT_SUPPORT_EXT.equals(importFileExt), Strings.lenientFormat("Illegal file type:[%s],expect:[zip]!", importFileExt));
        var prefixPath = Strings.lenientFormat("govern-service-%s-%s", System.currentTimeMillis(), importFilename);
        var importFile = File.createTempFile(prefixPath, ".temp");
        importZip.transferTo(importFile);

        try (ZipFile zipFile = new ZipFile(importFile)) {
            importResponse.setTotal(zipFile.size());
            Enumeration<?> entries = zipFile.entries();
            List<CompletableFuture<Boolean>> importFutures = new ArrayList<>(zipFile.size());
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String entryName = zipEntry.getName();
                if (entryName.startsWith(NACOS_DEFAULT_GROUP)) {
                    entryName = entryName.substring(NACOS_DEFAULT_GROUP.length());
                }
                if (entryName.contains("/")) {
                    entryName = entryName.replace("/", "-");
                }

                final String configId = entryName;

                try (var configStream = zipFile.getInputStream(zipEntry)) {
                    var configData = IOUtils.toString(configStream);
                    CompletableFuture<Boolean> setFuture;
                    switch (policy) {
                        case IMPORT_POLICY_OVERWRITE: {
                            setFuture = configService.setConfig(namespace, configId, configData);
                            break;
                        }
                        case IMPORT_POLICY_SKIP: {
                            setFuture = configService.containsConfig(namespace, configId).thenCompose(contained -> {
                                if (contained) {
                                    if (log.isInfoEnabled()) {
                                        log.info("importZip - Skip - [{}]@[{}] has contained.", configId, namespace);
                                    }
                                    return CompletableFuture.completedFuture(false);
                                }
                                return configService.setConfig(namespace, configId, configData);
                            });
                            break;
                        }
                        default:
                            throw new IllegalStateException("Unexpected policy[skip,overwrite] value: " + policy);
                    }
                    importFutures.add(setFuture);
                }
            }
            if (!importFutures.isEmpty()) {
                return CompletableFuture.allOf(importFutures.toArray(new CompletableFuture[importFutures.size()])).thenApply((nil) ->
                        {
                            int succeeded =(int) importFutures.stream().filter(future -> future.join()).count();
                            importResponse.setTotal(succeeded);
                            return importResponse;
                        }
                );
            }
            return CompletableFuture.completedFuture(importResponse);
        }

    }

    @PutMapping(RequestPathPrefix.CONFIGS_CONFIG)
    public CompletableFuture<Boolean> setConfig(@PathVariable String namespace, @PathVariable String configId, @RequestBody String data) {
        return configService.setConfig(namespace, configId, data);
    }


    @DeleteMapping(RequestPathPrefix.CONFIGS_CONFIG)
    public CompletableFuture<Boolean> removeConfig(@PathVariable String namespace, @PathVariable String configId) {
        return configService.removeConfig(namespace, configId);
    }

    @GetMapping(RequestPathPrefix.CONFIGS_CONFIG)
    public CompletableFuture<Config> getConfig(@PathVariable String namespace, @PathVariable String configId) {
        return configService.getConfig(namespace, configId);
    }

    @PutMapping(RequestPathPrefix.CONFIGS_CONFIG_TO)
    public CompletableFuture<Boolean> rollback(@PathVariable String namespace, @PathVariable String configId, @PathVariable int targetVersion) {
        return configService.rollback(namespace, configId, targetVersion);
    }

    @GetMapping(RequestPathPrefix.CONFIGS_CONFIG_VERSIONS)
    public CompletableFuture<List<ConfigVersion>> getConfigVersions(@PathVariable String namespace, @PathVariable String configId) {
        return configService.getConfigVersions(namespace, configId);
    }

    @GetMapping(RequestPathPrefix.CONFIGS_CONFIG_VERSIONS_VERSION)
    public CompletableFuture<ConfigHistory> getConfigHistory(@PathVariable String namespace, @PathVariable String configId, @PathVariable int version) {
        return configService.getConfigHistory(namespace, configId, version);
    }

}