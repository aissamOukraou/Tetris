package  tetris.VueControleur;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import  tetris.Tetris;

/**
 * Represents a properties file with the last window states.
 */
public class WindowStateFX extends java.util.Properties {

	private static final long serialVersionUID = -1824390546690585056L;
	
	// Singleton instance
	private final static WindowStateFX _instance = new WindowStateFX();

	// windows state file path
	static private final String propertiesPath = "./var/";
	private final Path _folderPath = FileSystems.getDefault().getPath(propertiesPath);
	static private final String propertiesFile = "windowstate.guid";
	private final Path _filePath = FileSystems.getDefault().getPath(propertiesPath, propertiesFile);
	
	/**
	 * Tetris WindowsStateFX is a Singleton so use getInstance()
	 * @return WindowsStateFX instance
	 */
	public static WindowStateFX getInstance() {
		return _instance;
	}
	
	private WindowStateFX() {
		super();

		// Check if folder exists and if not try to create it.
		if (!Files.exists(_folderPath)) {
			Tetris.minorError(String.format(
					"While reading windows state file: Path %s could not be found. Trying to create it."
					,_folderPath.toString()
					));
			try {
				Files.createDirectories(_folderPath);
			} catch (IOException e) {
				Tetris.fatalError(String.format(
						"While reading windows state file: Path %s could not be found. Trying to create it."
						,_folderPath.toString()
						));
			}
		}

		// Check if file exists and if not create a empty file
		if (Files.notExists(_filePath, LinkOption.NOFOLLOW_LINKS)) {
			Tetris.minorError(String.format(
					"While reading windows state file: File %s could not be found. Trying to create it."
					,_filePath.getFileName().toString()
					));
			try {
				Files.createFile(_filePath);
			} catch (IOException e) {
				Tetris.fatalError(String.format(
						"While reading windows state file: File %s could not be found. Trying to create it."
						,_filePath.getFileName().toString()
						));
			}
		}

		InputStream in = null;
		try {
			in = Files.newInputStream(_filePath);
			this.load(in);
		} catch (FileNotFoundException e) {
			Tetris.minorError("While reading windows state file: File " + propertiesFile + " not found!");
		} catch (IOException e) {
			Tetris.criticalError("While reading windows state file: File " + propertiesFile + " could not be loaded!");
		} finally {
			if (in!=null) {
				try {
					in.close();
				} catch (IOException e) {/* ignore */ }
			}
		}
	}

	/**
	 * Save the window states into a properties file.
	 */
	public void save() {
		OutputStream out=null;
		try {
			out = Files.newOutputStream(_filePath);
			this.store(out, " Window state file for Tetris");
		} catch (FileNotFoundException e) {
			Tetris.criticalError("While reading windows state file: File " + propertiesFile + " could not be saved!");
			e.printStackTrace();
		} catch (IOException e) {
			Tetris.criticalError("While reading windows state file: File " + propertiesFile + " could not be saved!");
			e.printStackTrace();
		} finally {
			if (out!=null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

}
