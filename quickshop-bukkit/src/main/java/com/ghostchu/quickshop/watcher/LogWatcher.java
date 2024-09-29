package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogWatcher implements AutoCloseable, Runnable {

  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter LOG_FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
  private final Queue<String> logs = new ConcurrentLinkedQueue<>();

  private WrappedTask task = null;

  private PrintWriter printWriter = null;

  public LogWatcher(final QuickShop plugin, final File log) {

    try {
      boolean deleteFailed = false;
      if(!log.exists()) {
        //noinspection ResultOfMethodCallIgnored
        log.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        log.createNewFile();
      } else {
        if((log.length() / 1024f / 1024f) > plugin.getConfig().getDouble("logging.file-size")) {
          final Path logPath = plugin.getDataFolder().toPath().resolve("logs");
          Files.createDirectories(logPath);
          //Find a available name
          Path targetPath;
          int i = 1;
          do {
            targetPath = logPath.resolve(ZonedDateTime.now().format(LOG_FILE_FORMATTER) + "-" + i + ".log.gz");
            i++;
          } while(Files.exists(targetPath));
          Files.createFile(targetPath);
          final GzipParameters gzipParameters = new GzipParameters();
          gzipParameters.setFilename(log.getName());
          try(GzipCompressorOutputStream archiveOutputStream = new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(targetPath.toFile())), gzipParameters)) {
            Files.copy(log.toPath(), archiveOutputStream);
            archiveOutputStream.finish();
            if(log.delete()) {
              //noinspection ResultOfMethodCallIgnored
              log.createNewFile();
            } else {
              deleteFailed = true;
            }
          }
        }
      }
      final FileWriter logFileWriter;
      if(deleteFailed) {
        //If could not delete, just override it
        logFileWriter = new FileWriter(log, false);
      } else {
        //Otherwise append
        logFileWriter = new FileWriter(log, true);
      }
      printWriter = new PrintWriter(logFileWriter);
    } catch(FileNotFoundException e) {
      plugin.logger().error("Log file was not found!", e);
    } catch(IOException e) {
      plugin.logger().error("Could not create the log file!", e);
    }
  }

  public void start(final int i, final int i2) {

    task = QuickShop.folia().getImpl().runTimerAsync(this, i, i2);
  }

  public void stop() {

    try {
      if(task != null && !task.isCancelled()) {
        task.cancel();
      }
    } catch(IllegalStateException ex) {
      Log.debug("Task already cancelled " + ex.getMessage());
    }
  }

  @Override
  public void close() {

    if(printWriter != null) {
      printWriter.flush();
      printWriter.close();
    }
  }

  public void log(@NotNull final String log) {

    logs.add("[" + DATETIME_FORMATTER.format(Instant.now()) + "] " + log);
  }

  @Override
  public void run() {

    if(printWriter == null) {
      //Waiting for init
      return;
    }
    final Iterator<String> iterator = logs.iterator();
    while(iterator.hasNext()) {
      final String log = iterator.next();
      printWriter.println(log);
      iterator.remove();
    }
    printWriter.flush();
  }

}
