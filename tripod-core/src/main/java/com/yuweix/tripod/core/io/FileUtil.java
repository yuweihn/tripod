package com.yuweix.tripod.core.io;


import com.yuweix.tripod.core.DateUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 文件处理工具
 * @author yuwei
 */
public abstract class FileUtil extends StreamUtil {
	private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
	/**
	 * 读取文件内容
	 * @param filePath
	 */
	public static String getContentFromFile(String filePath) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader bf = null;
		try {
			fis = new FileInputStream(filePath);
			isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			bf = new BufferedReader(isr);
			StringBuilder builder = new StringBuilder("");
			String line;
			do {
				line = bf.readLine();
				if (line != null) {
					if (builder.length() != 0) {
						builder.append("\n");
					}
					builder.append(line);
				}
			} while (line != null);
			return builder.toString();
		} catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e);
		} finally {
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 保存文件，返回存放路径+文件全名。
	 * @param content    文件内容
	 * @param rootDir    存放文件的一级目录
	 * @param subDir     存放文件的二级目录
	 * @param fileName   文件名(含扩展名)
	 * 
	 * eg.
	 * rootDir: /wx-resource
	 * subDir: /user
	 * fileName: 8149a013aef24114a8e3d10ba2ea5f1f.txt
	 * 返回：如"/user/8149a013aef24114a8e3d10ba2ea5f1f.txt"
	 */
	public static String write(byte[] content, String rootDir, String subDir, String fileName) {
		subDir = subDir == null || subDir.trim().equals("") ? "" : subDir.trim();
		String dateDir = DateUtil.formatDate(new Date(), DateUtil.PATTERN_DATE2);
		subDir = subDir + "/" + dateDir;

		write(content, rootDir + subDir + "/" + fileName);
		return subDir + "/" + fileName;
	}

	/**
	 * 保存文件
	 * @param content
	 * @param fullFileName    文件全路径
	 */
	public static void write(byte[] content, String fullFileName) {
		File file = new File(fullFileName);
		File dir = file.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		OutputStream fos = null;
		OutputStream bos = null;
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(content);
		} catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * /.     ===>   /
	 * /..    ===>   /
	 * /path/.   ===>   /path
	 * /path/..  ===>   /
	 */
	public static String simplifyPath(String path) {
		if (path == null) {
			return null;
		}
		path = path.trim();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if ("/".equals(path)) {
			return path;
		}
		if ("/.".equals(path) || "/..".equals(path)) {
			return "/";
		}
		if (path.endsWith("/.")) {
			return path.substring(0, path.lastIndexOf("/."));
		}
		if (path.endsWith("/..")) {
			String tempPath = path.substring(0, path.lastIndexOf("/.."));
			if (tempPath.lastIndexOf("/") <= 0) {
				return "/";
			} else {
				return tempPath.substring(0, tempPath.lastIndexOf("/"));
			}
		}
		return path;
	}
}
