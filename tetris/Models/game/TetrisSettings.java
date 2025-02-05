
package tetris.Models.game;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Properties;

import tetris.Tetris;

public class TetrisSettings extends Properties {

	private static final long serialVersionUID = 1649379748056477592L;

	// Singleton instance
	private final static TetrisSettings _instance = new TetrisSettings();

	// Default properties file
	static private final String folderPathPlain = "./var/";
	private final Path _folderPath = FileSystems.getDefault().getPath(folderPathPlain);
	static private final String fileNamePlain = "tetris.settings";
	private final Path _filePath = FileSystems.getDefault().getPath(folderPathPlain, fileNamePlain);

	/**
	 * TetrisSettings is a Singleton so use getInstance()
	 * @return TetrisSettings instance
	 */
	public static TetrisSettings getInstance() {
		return _instance;
	}

	private TetrisSettings() {
		// -- call constructor of java.util.Properties
		super();

		// Check if folder exists and if not try to create it.
		if (!Files.exists(_folderPath)) {
			Tetris.minorError(String.format(
					"While reading settings file: Path %s could not be found. Trying to create it."
					,_folderPath.toString()
					));
			try {
				Files.createDirectories(_folderPath);
			} catch (IOException e) {
				Tetris.fatalError(String.format(
						"While reading settings file: Path %s could not be found. Trying to create it."
						,_filePath.toString()
						));
			}
		}

		// Check if file exists and if not create a empty file
		if (Files.notExists(_filePath, LinkOption.NOFOLLOW_LINKS)) {
			Tetris.minorError(String.format(
					"While reading settings file: File %s could not be found. Trying to create it."
					,_filePath.getFileName().toString()
					));
			try {
				Files.createFile(_filePath);
			} catch (IOException e) {
				Tetris.fatalError(String.format(
						"While reading settings file: File %s could not be found. Trying to create it."
						,_filePath.getFileName().toString()
						));
			}
		}

		InputStream in = null;
		try {
			in = Files.newInputStream(_filePath);
			load(in);
		} catch (FileNotFoundException e) {
			Tetris.minorError("While reading settings file: File " + _filePath.toString() + " not found! Will be created on exit.");
		} catch (IOException e) {
			Tetris.criticalError("While reading settings file: File " + _filePath.toString() + " could not be loaded! Will be created on exit.");
		} finally {
			if (in!=null) {
			try {
					in.close();
				} catch (IOException e) {/*ignore*/}
			}
		}
	}

	/**
	 * Save properties into a properties file.
	 */
	public void save() {
		OutputStream out=null;
		try {
			out = Files.newOutputStream(_filePath);
			this.store(out, " Window state file for Tetris by Frank Kopp");
		} catch (FileNotFoundException e) {
			Tetris.criticalError("While reading settings file: File " + _filePath.toString() + " could not be saved!");
			e.printStackTrace();
		} catch (IOException e) {
			Tetris.criticalError("While reading settings file: File " + _filePath.toString() + " could not be saved!");
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
