package org.jvnet.hudson.update_center;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.RepositoryHandle;

public class ArtifactoryRepository extends LocalDirectoryRepository {

	public ArtifactoryRepository(String id, URL url, boolean includeSnapshots, File downloadDir) throws IOException {
		super(loadIndex(id, url), url, includeSnapshots, downloadDir);
	}

	
	private static File loadIndex(String id, URL url) throws IOException {
		File dir = new File(new File(System.getProperty("java.io.tmpdir")), "maven-index/" + id);
		File local = new File(dir, "index");

		Artifactory artifactory = ArtifactoryClientBuilder.create().setUrl(url.toString()).build();

		RepositoryHandle repository = artifactory.repository(id);

		List<org.jfrog.artifactory.client.model.File> searchItems = artifactory.searches().repositories(id)
				.artifactsByName("*.hpi").doSearch().stream().map(rp -> {
					org.jfrog.artifactory.client.model.File file = repository.file(rp.getItemPath()).info();
					return file;
				}).collect(Collectors.toList());

		if (!searchItems.isEmpty()) {
			long lastModified = searchItems.stream()
					.sorted((s1, s2) -> s2.getLastModified().compareTo(s1.getLastModified())).findFirst().get()
					.getLastModified().getTime();

			if (!local.exists() || (local.lastModified() != lastModified)) {
				System.out.println("Downloading " + url);
				// if the download fail in the middle, only leave a broken tmp file
				dir.mkdirs();
				File tmp = new File(dir, "index_");

				if (local.exists())
					FileUtils.deleteDirectory(local);
				local.mkdirs();

				searchItems.forEach(hpi -> {
					try {
						downloadHpi(repository, tmp, hpi);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				// as a proof that the expansion was properly completed
				tmp.renameTo(local);
				local.setLastModified(lastModified);
			} else {
				System.out.println("Reusing the locally cached " + url + " at " + local);
			}
		}

		return local;
	}

	private static void downloadHpi(RepositoryHandle repository, File dir, org.jfrog.artifactory.client.model.File hpiFile) throws IOException {
		try (InputStream is = repository.download(hpiFile.getPath()).doDownload()) {
			File outFile = new File(dir, hpiFile.getPath());
			
			outFile.getParentFile().mkdirs();
			java.nio.file.Files.copy(is, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

	}
}
