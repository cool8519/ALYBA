package dal.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.mozilla.universalchardet.UniversalDetector;

public class FileUtil {

	public static final class TYPE {
		public static final int FILE = 1;
		public static final int DIRECTORY = 2;
		public static final int BOTH = 3;
	}

	public static List<File> getMatchFiles(String in_pattern) {
		List<File> files = new ArrayList<File>();
		String fsep = File.separator;
		String pattern;
		try {
			if(fsep.equals("/")) {
				pattern = in_pattern.replace("\\", "/");
			} else {
				pattern = in_pattern.replace("/", "\\");
			}
			if(!pattern.startsWith(".") && !pattern.startsWith(fsep) && !(fsep.equals("\\") && pattern.charAt(1) == ':')) {
				pattern = "." + fsep + pattern;
			}

			String dir_pattern = pattern.substring(0, pattern.lastIndexOf(fsep));
			String file_pattern = pattern.substring(pattern.lastIndexOf(fsep) + 1);

			List<File> dirs = getMatchDirectories(dir_pattern);
			file_pattern = file_pattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*").replaceAll("\\?", ".");

			String dirname;
			List<File> dirfiles;
			for(int i = 0; i < dirs.size(); i++) {
				dirname = ((File)dirs.get(i)).getPath();
				dirfiles = getAllFiles(dirname, file_pattern, false);
				files.addAll(dirfiles);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return files;
	}

	public static List<File> getMatchDirectories(String dir_pattern) {
		List<File> dirs = new ArrayList<File>();
		String fsep = File.separator;
		String curr_dir = dir_pattern.startsWith(fsep) ? fsep : "";
		StringTokenizer st = new StringTokenizer(dir_pattern, fsep);
		String dname, dname_org;
		boolean flag = false;
		while(st.hasMoreTokens()) {
			dname_org = st.nextToken();
			if(!dname_org.equals(".") && !dname_org.equals("..")) {
				dname = dname_org.replaceAll("\\.", "\\\\.");
			} else {
				dname = dname_org;
			}
			List<File> subdir = null;
			if(dname.contains("?") || dname.contains("*")) {
				flag = true;
				dname = dname.replaceAll("\\*", ".*").replaceAll("\\?", ".");
				subdir = getAllDirectories(curr_dir, dname, false);
				String dir_temp;
				for(int i = 0; i < subdir.size(); i++) {
					int rear_idx = dir_pattern.indexOf(fsep, curr_dir.length());
					String rear = (rear_idx > -1) ? dir_pattern.substring(rear_idx) : "";
					dir_temp = ((File)subdir.get(i)).getPath() + rear;
					dirs.addAll(getMatchDirectories(dir_temp));
				}
			}
			curr_dir += dname_org + fsep;
		}
		if(!flag) {
			dirs.add(new File(dir_pattern));
		}
		return dirs;
	}

	public static List<File> getAllFiles(String path, String name_pattern, boolean recursive) {
		return getAllFiles(path, name_pattern, recursive, TYPE.FILE);
	}

	public static List<File> getAllDirectories(String path, String name_pattern, boolean recursive) {
		return getAllFiles(path, name_pattern, recursive, TYPE.DIRECTORY);
	}

	public static List<File> getAllFilesAndDirectories(String path, String name_pattern, boolean recursive) {
		return getAllFiles(path, name_pattern, recursive, TYPE.BOTH);
	}

	public static List<File> getAllFiles(String path, String name_pattern, boolean recursive, int type) {
		String regex_pattern = name_pattern;
		if(regex_pattern != null) {
			regex_pattern = regex_pattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*").replaceAll("\\?", ".");
		}
		List<File> fileList = new ArrayList<File>();
		try {
			File f_path = new File(path);
			File files[] = f_path.listFiles();
			if(files == null)
				return fileList;
			String fname;
			for(int i = 0; i < files.length; i++) {
				try {
					if(type == TYPE.BOTH || (files[i].isFile() && type == TYPE.FILE) || (files[i].isDirectory() && type == TYPE.DIRECTORY)) {
						fname = files[i].getName();
						if(name_pattern == null || name_pattern.equals("") || fname.matches(regex_pattern)) {
							fileList.add(files[i]);
						}
					}
					if(files[i].isDirectory() && recursive) {
						fileList.addAll(getAllFiles(files[i].getAbsolutePath(), name_pattern, recursive, type));
					}
				} catch(SecurityException se) {
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return fileList;
	}

	public static boolean isMatchFile(String path, String name_pattern) {
		String regex_pattern = name_pattern;
		if(regex_pattern != null) {
			regex_pattern = regex_pattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*").replaceAll("\\?", ".");
		}
		if(path != null && path.matches(regex_pattern)) {
			return true;
		}
		return false;
	}

	public static String getFileSizeString(File f) {
		if(f == null)
			return null;
		DecimalFormat df = new DecimalFormat("#,###");
		return df.format(f.length());
	}

	public static String readFileLine(File f, int line, String file_encoding) {
		if(f == null)
			return null;
		try {
			List<String> result = readFileLine(new FileInputStream(f), line, 1, file_encoding);
			if(result.size() > 0) {
				return (String)(result.get(0));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> readFileLine(File f, int from_line, int line, String file_encoding) {
		if(f == null)
			return null;
		try {
			return readFileLine(new FileInputStream(f), from_line, line, file_encoding);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> readFileLine(FileInputStream fis, int from_line, int line) {
		return readFileLine(fis, from_line, line, null);
	}

	public static List<String> readFileLine(FileInputStream fis, int from_line, int line, String file_encoding) {
		if(fis == null)
			return null;
		BufferedReader br = null;
		List<String> result = null;
		try {
			result = new ArrayList<String>();
			br = new BufferedReader(file_encoding==null ? new InputStreamReader(fis) : new InputStreamReader(fis, file_encoding));
			int cnt = 1;
			int getLines = 0;
			String s;
			while((s = br.readLine()) != null) {
				if(cnt >= from_line) {
					result.add(s);
					getLines++;
				}
				if(getLines == line) {
					break;
				}
				cnt++;
			}
		} catch(Exception e) {
			result = null;
		} finally {
			try {
				br.close();
			} catch(Exception e2) {
			}
		}
		return result;
	}
	
	public static List<String> head(File f, int line, String file_encoding) {
		if(f == null || line < 0) {
			return null;
		}
		try {
			return readFileLine(new FileInputStream(f), 0, line, file_encoding);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> tail(File f, int line, String file_encoding) {
		if(f == null || line < 0) {
			return null;
		}
		RandomAccessFile file = null;
		try {
			List<String> lst = new ArrayList<String>();
			file = new RandomAccessFile(f, "r");
			long fileSize = file.length();
			if(fileSize > 0) {
				long pos = fileSize - 1;
				int lineCnt = 0;
				while(true) {
					file.seek(pos);
					if(file.readByte() == '\n') {
						lineCnt++;
						if(lineCnt > line) {
							break;
						}
					}
					pos--;
					if(pos < 0) {
						break;
					}
				}
				file.seek(pos+1);
				for(int i = 0; i < line; i++) {
					String s = file.readLine();
					if(file_encoding == null) {
						lst.add(s);
					} else {
						lst.add(new String(s.getBytes("ISO-8859-1"), file_encoding));
					}
				}
			}
			return lst;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(file != null) {
				try {
					file.close();
				} catch(IOException ioe) {					
				}
			}
		}
		return null;		
	}

	public static void writeBinaryFile(File f, byte[] bytes, boolean append) {
		if(f == null || bytes == null)
			return;
		try {
			writeBinaryFile(new FileOutputStream(f), bytes, append);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeBinaryFile(OutputStream os, byte[] bytes, boolean append) {
		if(os == null)
			return;
		if(bytes == null) {
			try {
				os.close();
			} catch(Exception e) {
			}
			return;
		}
		try {
			os.write(bytes);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch(Exception e2) {
			}
		}
	}

	public static byte[] readBinaryFile(File f) {
		if(f == null)
			return null;
		byte[] bytes = new byte[(int)f.length()];
		try {
			if(readBinaryFile(new FileInputStream(f), bytes) > 0) {
				return bytes;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int readBinaryFile(InputStream is, byte[] bytes) {
		if(is == null)
			return -1;
		int idx = 0;
		try {
			int c;
			while((c = is.read()) != -1) {

				bytes[idx++] = (byte)c;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch(Exception e2) {
			}
		}
		return idx;
	}

	public static void writeObjectFile(File f, List<Object> objs, boolean append) {
		if(f == null || objs == null)
			return;
		try {
			writeObjectFile(new FileOutputStream(f), objs, append);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeObjectFile(OutputStream os, List<Object> objs, boolean append) {
		if(os == null)
			return;
		if(objs == null) {
			try {
				os.close();
			} catch(Exception e) {
			}
			return;
		}
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(os);
			for(Object o : objs) {
				out.writeObject(o);
			}
			out.flush();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch(Exception e2) {
			}
		}
	}

	public static List<Object> readObjectFile(File f) {
		if(f == null)
			return null;
		try {
			return readObjectFile(new FileInputStream(f));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<Object> readObjectFile(InputStream is) {
		if(is == null)
			return null;
		List<Object> objs = new ArrayList<Object>();
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(is);
			Object obj = null;
			while((obj = in.readObject()) != null) {
				objs.add(obj);
			}
		} catch(EOFException eof) {
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch(Exception e2) {
			}
		}
		return objs;
	}

	public static Properties readProperties(String filename) throws Exception {
		return readProperties(new FileInputStream(filename));
	}

	public static Properties readProperties(InputStream is) throws Exception {
		Properties props = new Properties();
		try {
			props.load(is);
		} catch(IOException ioexception) {
			throw new Exception(ioexception);
		}
		return props;
	}

	public static File createTemporaryFile(InputStream is, String prefix, String suffix, File directory, boolean deleteOnExit) {
		if(is == null) {
			return null;
		}
		File tempFile = null;
		BufferedOutputStream os = null;
		try {
			tempFile = File.createTempFile(prefix, suffix, directory);
			os = new BufferedOutputStream(new FileOutputStream(tempFile));
			int rbytes = 0;
			byte[] buff = new byte[4096];
			while((rbytes = is.read(buff, 0, 4096)) != -1) {
				os.write(buff, 0, rbytes);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(is != null)
				try {
					is.close();
				} catch(Exception e) {
				}
			;
			if(os != null)
				try {
					os.close();
				} catch(Exception e) {
				}
			;
			if(deleteOnExit)
				tempFile.deleteOnExit();
		}
		return tempFile;
	}

	public static File getFileFromResource(String resource_path) {
		try {
			URL dataURL = ClassLoader.getSystemResource(resource_path);
			return new File(dataURL.toURI());
		} catch(Exception e) {
			return null;
		}
	}
	
	public static String getFileEncoding(String file_path) {
		byte[] buf = new byte[4096];
		FileInputStream fis = null;
		UniversalDetector detector = null;
		try {
			fis = new FileInputStream(file_path);
			detector = new UniversalDetector(null);
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			detector.dataEnd();
			return detector.getDetectedCharset();
		} catch(Exception e) {
			return null;
		} finally {
			if(fis != null) {
				try { fis.close(); } catch(Exception e) {}
			}
			if(detector != null) {
				try { detector.reset(); } catch(Exception e) {}
			}
		}
	}	
	
	
}
