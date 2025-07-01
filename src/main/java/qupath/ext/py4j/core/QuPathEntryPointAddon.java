package qupath.ext.py4j.core;

import qupath.lib.gui.scripting.QPEx;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.imagej.images.servers.ImageJServer;
import qupath.lib.images.servers.bioformats.BioFormatsImageServer;
import qupath.lib.images.servers.openslide.OpenslideImageServer;
import qupath.lib.images.servers.ImageServerBuilder;
import com.google.gson.Gson;

import qupath.fx.utils.FXUtils;
import qupath.lib.gui.panes.ImageDetailsPane;
import qupath.lib.gui.prefs.PathPrefs;
import qupath.lib.gui.tools.GuiTools;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.ImageServers;
import qupath.lib.images.servers.openslide.OpenslideServerBuilder;
import qupath.lib.images.servers.bioformats.BioFormatsServerBuilder;
import qupath.imagej.images.servers.ImageJServerBuilder;
import qupath.lib.io.GsonTools;
import qupath.lib.projects.ProjectImageEntry;
import qupath.lib.projects.Project;
import qupath.lib.gui.commands.Commands;

import qupath.lib.projects.Projects;
import qupath.lib.projects.ProjectIO;
import qupath.lib.gui.commands.ProjectCommands;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.regions.RegionRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.util.Collection;


/**
 * Add methods I found useful to {@link QuPathEntryPoint}.
 * It will serve the main entry point of the extension.
 */
public class QuPathEntryPointAddon extends QuPathEntryPoint {

	/**
	 * Refresh the current project {@link QPEx#getProject() getProject()} in QuPath.
	 * This should be called whenever the current project has changed
	 * (e.g. by adding or removing items)
	 *
	 * @see QPEx#getProject()
	 * @see QuPathGUI#refreshProject()
	 */
	public static void refreshProjectInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			getQuPath().refreshProject();
			return null;
		});
	}

	/**
	 * Repaint the entire image in the current viewer
	 * {@link QPEx#getCurrentViewer() getCurrentViewer()} in QuPath.
	 * This should be called whenever a major change in display is triggered,
	 * such as changing the brightness/contrast or lookup table.
	 *
	 * @see QPEx#getCurrentViewer()
	 * @see QuPathViewer#repaintEntireImage()
	 */
	public static void repaintEntireImageInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			getCurrentViewer().repaintEntireImage();
			return null;
		});
	}

	/**
	 * Open project <code>project</code> in QuPath.
	 * It will become the current project
	 * and can be retrieved by {@link QPEx#getProject() getProject()}
	 *
	 * @param project the project to open
	 *
	 * @see QPEx#getProject()
	 * @see QuPathGUI#setProject(Project)
	 */
	public static void openProjectInQuPath(Project<BufferedImage> project) {
		FXUtils.callOnApplicationThread(() -> {
			getQuPath().setProject(project);
			return null;
		});
	}

	/**
	 * Close the current project {@link QPEx#getProject() getProject()} in QuPath.
	 * {@link QPEx#getProject() getProject()} will return <b>null</b> when this is done.
	 *
	 * @see QPEx#getProject()
	 * @see Commands#closeProject(QuPathGUI)
	 */
	public static void closeProjectInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			Commands.closeProject(getQuPath());
			return null;
		});
	}

	/**
	 * Open image <code>imageData</code> in the current viewer
	 * {@link QPEx#getCurrentViewer() getCurrentViewer()} in QuPath.
	 * It will become the current image data and
	 * can be retrieved by {@link QPEx#getCurrentImageData() getCurrentImageData()}.
	 *
	 * <p>
	 *     If the current project contains <code>imageData</code>,
	 *     the associated {@link ProjectImageEntry} will become the current project entry
	 *     and can be retrieved by {@link QPEx#getProjectEntry() getProjectEntry()}.
	 *     Otherwise, the current project will be closed.
	 * </p>
	 *
	 * @param imageData the image to open
	 *
	 * @see QPEx#getCurrentImageData()
	 * @see QPEx#getProjectEntry()
	 * @see QuPathViewer#setImageData(ImageData)
	 */
	public static void openImageDataInQuPath(ImageData<BufferedImage> imageData) {
		FXUtils.callOnApplicationThread(() -> {
//			saveCurrentImageData();
			if ((getProject() != null) && (getProject().getEntry(imageData) == null)) {
				closeProjectInQuPath();
			}
			getCurrentViewer().setImageData(imageData);
			getQuPath().refreshProject();
			return null;
		});
	}

	/**
	 * Close the current image data {@link QPEx#getCurrentImageData() getCurrentImageData()}
	 * in QuPath. {@link QPEx#getCurrentImageData() getCurrentImageData()} will return <b>null</b>
	 * when this is done.
	 *
	 * <p>
	 *     The associated project entry will become "inactive". That is,
	 *     {@link QPEx#getProjectEntry() getProjectEntry()} will return <b>null</b>.
	 * </p>
	 *
	 * @see QPEx#getCurrentImageData()
	 * @see QPEx#getProjectEntry()
	 * @see QuPathViewer#resetImageData()
	 */
	public static void closeImageDataInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
//			saveCurrentImageData();
			getCurrentViewer().resetImageData();
			getQuPath().refreshProject();
			return null;
		});
	}

	/**
	 * Set the available {@link PathClass}es of both {@link QPEx#getQuPath() getQuPath()}
	 * and {@link QPEx#getProject() getProject()}.
	 *
	 * @param pathClasses The collection of PathClass objects to set.
	 * @param refreshProject If true, the project's path classes will also be updated.
	 *                       This is useful when the project is currently open and
	 *                       you want to ensure its internal state reflects the new path classes.
	 *                       If false, only the GUI's available path classes are updated.
	 *
	 * @see Project#setPathClasses(Collection)
	 * @see QuPathGUI#getAvailablePathClasses()
	 */
	public static void setPathClassesInQuPath(
			Collection<? extends PathClass> pathClasses,
			boolean refreshProject) {
		FXUtils.callOnApplicationThread(() -> {    // set the path classes of the current QuPath
			getQuPath().getAvailablePathClasses().setAll(pathClasses);
			return null;
		});
		if (refreshProject && (getProject() != null)) {
			getProject().setPathClasses(pathClasses);  // set the path classes of the current project
		}
	}

	/**
	 * Reset the available {@link PathClass}es of both {@link QPEx#getQuPath() getQuPath()}
	 * and {@link QPEx#getProject() getProject()} to their default values.
	 * 
	 * @param refreshProject If true, the project's path classes will also be updated.
	 *                       This is useful when the project is currently open and
	 *                       you want to ensure its internal state reflects the new path classes.
	 *
	 * @see Project#setPathClasses(Collection)
	 * @see QuPathGUI#resetAvailablePathClasses()
	 */
	public static void resetPathClassesInQuPath(boolean refreshProject) {
        // reset the path classes of the current QuPath to the default values
        var pathClasses = FXUtils.callOnApplicationThread(() -> {
            getQuPath().resetAvailablePathClasses();
            return getQuPath().getAvailablePathClasses();
        });
		if (refreshProject && (getProject() != null)) {
			getProject().setPathClasses(pathClasses);  // set the path classes of the current project
		}
	}

	/**
	 * Create a new {@link Project} at path <code>projectPath</code>.
	 * The existing contents of <code>projectPath</code> will be erased.
	 *
	 * @param projectPath the path to the project
	 * @return the created project
	 */
	public static Project<BufferedImage> createProject(String projectPath) {
		mkdirs(projectPath);
		return Projects.createProject(new File(projectPath), BufferedImage.class);
	}

	/**
	 * Save <code>project</code> back to its associated path.
	 *
	 * <p>
	 *     {@link Project} is created with its associated path
	 *     and has to be saved back to it.
	 * </p>
	 *
	 * @param project the project to save
	 * @throws IOException if an error occurs while saving the project
	 *
	 * @see Project#syncChanges()
	 * @see QuPathEntryPointAddon#createProject(String)
	 */
	public static void saveProject(Project<BufferedImage> project) throws IOException {
		if (project != null) {
			project.syncChanges();
		}
	}

	/**
	 * Load a {@link Project} from path <code>projectPath</code>.
	 * The project is defined by JSON file <code>projectPath/project.qpproj</code>.
	 *
	 * @param projectPath the path to the project
	 * @return the loaded project
	 * @throws IOException if an error occurs while loading the project
	 *
	 * @see ProjectIO#loadProject(File, Class)
	 */
	public static Project<BufferedImage> loadProject(String projectPath) throws IOException {
		File file = new File(projectPath);
		if (file.isDirectory()) {
			file = new File(file, ProjectIO.DEFAULT_PROJECT_NAME + ProjectIO.getProjectExtension(true));
		}
		return ProjectIO.loadProject(file, BufferedImage.class);
	}

	/**
	 * Add image <code>server</code> to <code>project</code> and
	 * return its {@link ProjectImageEntry}.
	 *
	 * <p>
	 *     Its {@link ImageData.ImageType ImageType} is estimated by
	 *     {@link QuPathEntryPointAddon#estimatedImageType} and
	 *     the thumbnail is refreshed by {@link QuPathEntryPointAddon#refreshThumbnail}
	 * </p>
	 *
	 * @param project the project to add the image entry to
	 * @param server  the image server used to create the image entry
	 * @return <code>entry</code> - the added image entry
	 * @throws IOException if an error occurs while adding the image entry
	 *
	 * @see ProjectCommands#addSingleImageToProject
	 */
	public static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			ImageServer<BufferedImage> server) throws IOException {
		var type  = estimatedImageType(server);
		var entry = ProjectCommands.addSingleImageToProject(project, server, type);
		refreshThumbnail(entry, server);	// refresh its thumbnail
		return entry;
	}

	/**
	 * Add image file <code>imagePath</code> to <code>project</code> and
	 * return its {@link ProjectImageEntry}.
	 *
	 * <p>
	 *     Its {@link ImageData.ImageType ImageType} is estimated by
	 *     {@link QuPathEntryPointAddon#estimatedImageType} and
	 *     the thumbnail is refreshed by {@link QuPathEntryPointAddon#refreshThumbnail}
	 * </p>
	 *
	 * @param project the project to add the image entry to
	 * @param imagePath  the image file used to create the image entry
	 * @return the added image entry
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 * @throws IOException if an error occurs while loading the image file
	 *
	 * @see ProjectCommands#addSingleImageToProject
	 * @see QuPathEntryPointAddon#createImageServer
	 */
	public static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			String imagePath) throws URISyntaxException, IOException {
		return addImageEntry(project, createImageServer(imagePath));
	}

	/**
	 * Remove image <code>entry</code> from <code>project</code>.
	 * All associated data will be removed.
	 *
	 * <p>
	 *     {@link ProjectImageEntry} is part  of {@link Project} and
	 *     will be deleted when it is removed. The only thing remains
	 *     is the image server used to create it.
	 * </p>
	 * @param project the project to remove the image entry from
	 * @param entry   the image entry to remove
	 *
	 * @see Project#removeImage
	 */
	public static void removeImageEntry(
			Project<BufferedImage> project,
			ProjectImageEntry<BufferedImage> entry) {
		project.removeImage(entry, true);
	}

	/**
	 * Create a new {@link ImageData} using image <code>server</code>.
	 * Its image type is estimated by {@link QuPathEntryPointAddon#estimatedImageType}.
	 *
	 * @param server the image server used to create the image data
	 * @return <code>imageData</code> - the created image data
	 *
	 * @see ImageData
	 */
	public static ImageData<BufferedImage> createImageData(ImageServer<BufferedImage> server) throws IOException {
		var type = estimatedImageType(server);
		return new ImageData<BufferedImage>(server, type);
	}

	/**
	 * Create a new {@link ImageData} using image file <code>imagePath</code>.
	 * Its image type is estimated by {@link QuPathEntryPointAddon#estimatedImageType}.
	 *
	 * @param imagePath the image file used to create the image data
	 * @return <code>imageData</code> - the created image data
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 * @throws IOException if an error occurs while loading the image file
	 *
	 * @see QuPathEntryPointAddon#createImageServer
	 */
	public static ImageData<BufferedImage> createImageData(String imagePath) throws URISyntaxException, IOException {
		return createImageData(createImageServer(imagePath));
	}

	/**
	 * Save <code>imageData</code> back to its <code>project</code>.
	 *
	 * @param imageData the image data to save
	 * @param project the project to save to
	 * @throws IOException if an error occurs while saving the image data
	 *
	 * @see Project#getEntry
	 * @see ProjectImageEntry#saveImageData
	 */
	public static void saveImageData(
			ImageData<BufferedImage> imageData,
			Project<BufferedImage> project) throws IOException {
		if ((project != null) && (imageData != null)) {
			var entry = project.getEntry(imageData);
			if (entry != null) {
				entry.saveImageData(imageData);
			}
		}
	}

	/**
	 * Save <code>imageData</code> back to the current project.
	 *
	 * @param imageData the image data to save
	 * @throws IOException if an error occurs while saving the image data
	 *
	 * @see QPEx#getProject()
	 */
	public static void saveImageData(
			ImageData<BufferedImage> imageData) throws IOException {
		saveImageData(imageData, getProject());
	}

	/**
	 * Save the current image data
	 *
	 * @throws IOException if an error occurs while saving the image data
	 *
	 * @see QPEx#getCurrentImageData()
	 */
	public static void saveCurrentImageData() throws IOException {
		saveImageData(getCurrentImageData());
	}

	/**
	 * Create a new {@link ImageJServer} using image file <code>imagePath</code>.
	 *
	 * @param imagePath the path to the image
	 * @return <code>imagejServer</code> - the created ImageJ image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 *
	 * @see ImageJServerBuilder#buildServer
	 * @see ImageJServer#dumpMetadata()
	 */
	public static ImageServer<BufferedImage> createImageJImageServer(String imagePath) throws URISyntaxException {
		return new ImageJServerBuilder().buildServer(toURI(imagePath));
	}

	/**
	 * Create a new {@link BioFormatsImageServer} using image file <code>imagePath</code>.
	 *
	 * @param imagePath the path to the image
	 * @return <code>bioformatsServer</code> - the created BioFormats image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 *
	 * @see BioFormatsServerBuilder#buildServer
	 * @see BioFormatsImageServer#dumpMetadata()
	 */
	public static ImageServer<BufferedImage> createBioFormatsImageServer(String imagePath) throws URISyntaxException {
		return new BioFormatsServerBuilder().buildServer(toURI(imagePath));
	}

	/**
	 * Create a new {@link OpenslideImageServer} using image file <code>imagePath</code>.
	 *
	 * @param imagePath the path to the image file
	 * @return <code>openslideServer</code> - the created Openslide image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 *
	 * @see OpenslideServerBuilder#buildServer
	 * @see OpenslideImageServer#dumpMetadata()
	 */
	public static ImageServer<BufferedImage> createOpenslideImageServer(String imagePath) throws URISyntaxException {
		return new OpenslideServerBuilder().buildServer(toURI(imagePath));
	}

	/**
	 * Create a new {@link ImageServer} using image file <code>imagePath</code>.
	 *
	 * <p>
	 *     QuPath will decide which {@link ImageServerBuilder}
	 *     to use to read <code>imagePath</code>.
	 * </p>
	 *
	 * @param imagePath the path to the image file
	 * @return <code>server</code> - the created image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 * @throws IOException if an error occurs while loading the image file
	 *
	 * @see ImageServers#buildServer
	 */
	public static ImageServer<BufferedImage> createImageServer(String imagePath) throws URISyntaxException, IOException {
		return ImageServers.buildServer(toURI(imagePath));
	}

	/**
	 * Save image <code>server</code> to JSON file <code>jsonServerPath</code>.
	 *
	 * @param server         the image server to save
	 * @param jsonServerPath the path to save image server
	 * @throws IOException if an error occurs while saving the image server
	 *
	 * @see FileWriter#write(String)
	 * @see GsonTools#getInstance()
	 * @see Gson#toJson(Object)
	 */
	public static void saveImageServer(
			ImageServer<BufferedImage> server,
			String jsonServerPath) throws IOException {
		try (FileWriter writer = new FileWriter(jsonServerPath)) {
			String serverJson = GsonTools.getInstance().toJson(server);
			writer.write(serverJson);
		}
	}

	/**
	 * Load an {@link ImageServer} from JSON file <code>jsonServerPath</code>.
	 *
	 * @param jsonServerPath the path to the JSON file
	 * @return <code>server</code> - the loaded image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 * @throws IOException if an error occurs while loading the image file
	 *
	 * @see QuPathEntryPointAddon#createImageServer
	 */
	public static ImageServer<BufferedImage> loadImageServer(String jsonServerPath) throws URISyntaxException, IOException {
		return createImageServer(jsonServerPath);
	}

	/**
	 * Set the image type of image data <code>imageData</code>.
	 *
	 * <p>
	 * If the image type is not set, it will be estimated. <br>
	 * If the image type setting is set to {@link PathPrefs.ImageTypeSetting#PROMPT},
	 * a prompt will be displayed to set the image type. <br>
	 * If the image type setting is set to {@link PathPrefs.ImageTypeSetting#AUTO_ESTIMATE},
	 * the image type will be automatically set.
	 * </p>
	 * @param imageData the image data to set the image type
	 */
	public static void setImageType(ImageData<BufferedImage> imageData) throws IOException {
		if ((imageData != null) && (imageData.getImageType() == null || imageData.getImageType() == ImageData.ImageType.UNSET)) {
			var setType = PathPrefs.imageTypeSettingProperty().get();
			if (setType == PathPrefs.ImageTypeSetting.AUTO_ESTIMATE || setType == PathPrefs.ImageTypeSetting.PROMPT) {
				ImageData.ImageType type = estimatedImageType(imageData.getServer());
				if (setType == PathPrefs.ImageTypeSetting.PROMPT) {
					ImageDetailsPane.promptToSetImageType(imageData, type);
				} else {
					imageData.setImageType(type);
					imageData.setChanged(false); // Don't want to retain this as a change resulting in a prompt to save the data
				}
			}
		}
	}

	/**
	 * Estimate <code>server</code>'s {@link ImageData.ImageType}
	 * using its default thumbnail.
	 *
	 * <p>
	 *     {@link ImageData.ImageType ImageType} is a property belonging
	 *     to {@link ImageData} or {@link ProjectImageEntry}.
	 *     It specifies what kind of image it is (e.g. RGB, grayscale, ...)
	 * </p>
	 *
	 * @param server the image server to estimate the image type
	 * @return <code>imageType</code> - the estimated image type
	 * @throws IOException if an error occurs while estimating the image type
	 *
	 * @see ImageServer#getDefaultThumbnail
	 * @see GuiTools#estimateImageType
	 */
	public static ImageData.ImageType estimatedImageType(ImageServer<BufferedImage> server) throws IOException {
		return GuiTools.estimateImageType(server, server.getDefaultThumbnail(0,0));
	}

	/**
	 * Refresh <code>entry</code>'s thumbnail
	 * using <code>server</code>'s "computed" thumbnail.
	 *
	 * <p>
	 *     {@link ProjectImageEntry} does not have function like <code>getServer()</code>
	 *     to retrieve its associated image server.
	 *     It does provide {@link ProjectImageEntry#readImageData() readImageData()}
	 *     and we are supposed to be able to use {@link ImageData#getServer()}
	 *     to retrieve the associated image server.
	 *     The problem is that {@link ProjectImageEntry#readImageData() readImageData()}
	 *     is to read the "saved" image data (NOT THE CURRENT IMAGE DATA).
	 *     That's why we explicitly specify the image server to use to refresh the thumbnail.
	 * </p>
	 *
	 * @param entry  the image entry to refresh the thumbnail
	 * @param server the image server to get the thumbnail from
	 * @throws IOException if an error occurs while getting the thumbnail
	 *
	 * @see ProjectCommands#getThumbnailRGB(ImageServer)
	 * @see ProjectImageEntry#setThumbnail
	 */
	public static void refreshThumbnail(
			ProjectImageEntry<BufferedImage> entry,
			ImageServer<BufferedImage> server) throws IOException {
		entry.setThumbnail(ProjectCommands.getThumbnailRGB(server));
	}

	/**
	 * Convert a file path to a URI.
	 *
	 * <p>
	 *     If the path is already a URI string, convert it to a URI.
	 *     Otherwise, it will be converted to a file URI (ie add "file:").
	 * </p>
	 * @param imagePath the path to convert
	 * @return the URI
	 */
	private static URI toURI(String imagePath) throws URISyntaxException {
		if (imagePath.startsWith("file:") || imagePath.startsWith("http:") || imagePath.startsWith("https:")) {
			return new URI(imagePath);
		} else {
			return new File(imagePath).toURI();
		}
	}


//	/**
//	 * Read the first z-slice and first time point of
//	 * the provided image at the provided downsample and
//	 * return an image in the 'imagej tiff' format.
//	 *
//	 * @param server     the image server to read from
//	 * @param downsample the downsample to use when reading the image
//	 * @return a byte array representing the requested image in the 'imagej tiff' format
//	 * @throws IOException when an error occurs while reading the image
//	 *
//	 * @see QuPathEntryPoint#getImageBytes(ImageServer, double, String)
//	 */
//	public static byte[] getImage(
//			ImageServer<BufferedImage> server,
//			double downsample) throws IOException {
//		return getImageBytes(server, downsample, "imagej tiff");
//	}
//
//	/**
//	 * Read the first z-slice and first time point of
//	 * a portion of the provided image at the provided downsample and
//	 * return an image in the 'imagej tiff' format.
//	 *
//	 * @param server     the image server to read from
//	 * @param downsample the downsample to use when reading the image
//	 * @param x          the x-coordinate of the portion of the image to retrieve
//	 * @param y          the y-coordinate of the portion of the image to retrieve
//	 * @param width      the width of the portion of the image to retrieve
//	 * @param height     the height of the portion of the image to retrieve
//	 * @return a byte array representing the requested image in the 'imagej tiff' format
//	 * @throws IOException when an error occurs while reading the image
//	 *
//	 * @see QuPathEntryPoint#getImageBytes(ImageServer, double, int, int, int, int, String)
//	 */
//	public static byte[] getImage(
//			ImageServer<BufferedImage> server,
//			double downsample,
//			int x, int y, int width, int height) throws IOException {
//		return getImageBytes(server, downsample, x, y, width, height, "imagej tiff");
//	}
//
//	/**
//	 * Read a portion of the provided image at the provided downsample and
//	 * return an image in the 'imagej tiff' format.
//	 *
//	 * @param server     the image server to read from
//	 * @param downsample the downsample to use when reading the image
//	 * @param x          the x-coordinate of the portion of the image to retrieve
//	 * @param y          the y-coordinate of the portion of the image to retrieve
//	 * @param width      the width of the portion of the image to retrieve
//	 * @param height     the height of the portion of the image to retrieve
//	 * @param z          the z-slice of the image to retrieve
//	 * @param t          the time point of the image to retrieve
//	 * @return a byte array representing the requested image in the 'imagej tiff' format
//	 * @throws IOException when an error occurs while reading the image
//	 *
//	 * @see QuPathEntryPoint#getImageBytes(ImageServer, double, int, int, int, int, int, int, String)
//	 */
//	public static byte[] getImage(
//			ImageServer<BufferedImage> server,
//			double downsample,
//			int x, int y, int width, int height,
//			int z, int t) throws IOException {
//		return getImageBytes(server, downsample, x, y, width, height, z, t, "imagej tiff");
//	}
//
//	/**
//	 * Read a portion of the provided image and
//	 * return an image in the 'imagej tiff' format.
//	 *
//	 * @param server  the image server to read from
//	 * @param request the region to read.
//	 * @return a byte array representing the requested image in the 'imagej tiff' format
//	 * @throws IOException when an error occurs while reading the image
//	 *
//	 * @see QuPathEntryPoint#getImageBytes(ImageServer, RegionRequest, String)
//	 */
//	public static byte[] getImage(
//			ImageServer<BufferedImage> server,
//			RegionRequest request) throws IOException {
//		return getImageBytes(server, request, "imagej tiff");
//	}
//	 * Add an image server <code>server</code> to project <code>project</code>
//	 * with the specified image type <code>type</code>.
//	 * An image entry is returned and its thumbnail is refreshed after adding it.
//	 *
//	 * @param project the project to add the image entry to
//	 * @param server  the image server used to create the image entry
//	 * @param type    {@link ImageData.ImageType} or null
//	 * @return the added image entry
//	 * @throws IOException if an error occurs while adding the image entry
//	 *
//	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
//	 *      ImageData.ImageType)
//	 */
//	private static ProjectImageEntry<BufferedImage> addImageEntry(
//			Project<BufferedImage> project,
//			ImageServer<BufferedImage> server,
//			ImageData.ImageType type) throws IOException {
//		var entry = ProjectCommands.addSingleImageToProject(project, server, type);
//		refreshThumbnail(entry, server);	// refresh its thumbnail
//		return entry;
//	}
//
//	/**
//	 * Add an image file <code>imagePath</code> to project <code>project</code>
//	 * with the specified image type <code>type</code>.
//	 * An image entry is returned and its thumbnail is refreshed after adding it.
//	 *
//	 * @param project   the project to add the image entry to
//	 * @param imagePath the image file used to create the image entry
//	 * @param type      {@link ImageData.ImageType} or null
//	 * @return the added image entry
//	 * @throws IOException if an error occurs while adding the image entry
//	 *
//	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
//	 *      ImageData.ImageType)
//	 */
//	private static ProjectImageEntry<BufferedImage> addImageEntry(
//			Project<BufferedImage> project,
//			String imagePath,
//			ImageData.ImageType type) throws IOException {
//		return addImageEntry(project, ImageServers.buildServer(imagePath), type);
//	}
//
//	/**
//	 * Create a new image data using image server <code>server</code>
//	 * with the specified image type <code>type</code>.
//	 *
//	 * @param server the image server to create the image data from
//	 * @param type the image type
//	 * @return the created image data
//	 */
//	private static ImageData<BufferedImage> createImageData(
//			ImageServer<BufferedImage> server,
//			ImageData.ImageType type) throws IOException {
//		return new ImageData<BufferedImage>(server, type);
//	}
//
//	/**
//	 * Create a new image data using image file <code>imagePath</code>
//	 * with the specified image type <code>type</code>.
//	 *
//	 * @param imagePath the image file to create the image data from
//	 * @param type the image type
//	 * @return the created image data
//	 *
//	 * @see ImageServers#buildServer(String, String...)
//	 */
//	private static ImageData<BufferedImage> createImageData(
//			String imagePath,
//			ImageData.ImageType type) throws IOException {
//		return createImageData(ImageServers.buildServer(imagePath), type);
//	}

//
//	 comment out these 3 methods
//	 1. use OpenImageDataInPath(imageData) to open the entry
//	 2. with imageData = entry.readImageData() <- this is how to get the imageData with GUI!
//	 3. this will ensure the same codes (GUI or not) -
//	    a. imageData == getCurrentImageData()
//	    b. imageData is the associated imageData of entry
//	 4. entry.saveImageData(imageData) to save your analysis results
//

//	/**
//	 * Open an image entry in QuPath.
//	 * <p>
//	 * If the current image data has been changed, it will be saved before opening
//	 * the new image entry.
//	 * </p>
//	 *
//	 * @param entry the image entry to open
//	 * @return true if the image entry was opened, false otherwise
//	 */
//	public static boolean openImageEntryInQuPath(ProjectImageEntry<BufferedImage> entry) {
//		return FXUtils.callOnApplicationThread(() -> {
//			saveCurrentImageData();
//			return getQuPath().openImageEntry(entry);
//		});
//	}

//	/**
//	 * Close the current image entry in QuPath.
//	 * <p>
//	 * If the current image data has been changed, it will be saved before closing
//	 * the image entry.
//	 * </p>
//	 */
//	public static void closeImageEntryInQuPath() {
//		FXUtils.callOnApplicationThread(() -> {
//			saveCurrentImageData();
//			getCurrentViewer().resetImageData();
//			refreshProject();
//			return null;
//		});
//	}

//	/**
//	 * Close the specified image entry in QuPath.
//	 * <p>
//	 * If the current image data has been changed, it will be saved before closing
//	 * the image entry.
//	 * </p>
//	 *
//	 * @param entry the image entry to close
//	 */
//	public static void closeImageEntryInQuPath(ProjectImageEntry<BufferedImage> entry) {
//		FXUtils.callOnApplicationThread(() -> {
//			if (entry == getProject().getEntry(getCurrentImageData())) {
//				closeImageEntryInQuPath();
//			}
//			return null;
//		});
//	}

}
