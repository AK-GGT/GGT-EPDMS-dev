package de.iai.ilcd.model.common;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;

import javax.persistence.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "xmlfile")
public class XmlFile implements Serializable {

    /**
     * The default name that the xmlFile entry will have by default inside the
     * archive
     */

    public static final String ZIP_DEFAULT_ENTRY_NAME = "default_xml";
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Compressed version of {@link #content}. Is being auto generated prior to
     * persist/merge
     *
     * @see #compressContent()
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] compressedContent;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return decompressContent();
    }

    public void setContent(String content) {
        compressContent(content);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.compressedContent == null) ? 0 : this.compressedContent.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof XmlFile)) {
            return false;
        }
        XmlFile other = (XmlFile) obj;
        if (this.compressedContent == null) {
            if (other.compressedContent != null) {
                return false;
            }
        } else if (!this.compressedContent.equals(other.compressedContent)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * Compress the content of XML file prior to persist/merge events in order to
     * save database space and be compatible with MySQL server default
     * configurations as long as possible (1MB max package size)
     *
     * @throws Exception if anything goes wrong, just in-memory IO operations,
     *                   should not happen
     * @see #decompressContent()
     */
    protected void compressContent(String s) {
        // create a zip archive with a single entry

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ZipArchiveOutputStream z = new ZipArchiveOutputStream(bs);

        try {
            z.putArchiveEntry(new ZipArchiveEntry(ZIP_DEFAULT_ENTRY_NAME));
            z.write(s.getBytes(StandardCharsets.UTF_8.name()));
            z.closeArchiveEntry();
            z.flush();
            z.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to compress xml to zip", e);
        }

        this.compressedContent = bs.toByteArray();
    }

    /**
     * Decompress content of XML file after loading. The reason for doing this, see
     * {@link #compressContent()}
     *
     * @throws Exception if anything goes wrong, just in-memory IO operations,
     *                   should not happen
     * @see #compressContent()
     */
    protected String decompressContent() {
        if (this.compressedContent == null || this.compressedContent.length == 0)
            return null;

        try (ZipFile z = new ZipFile(new SeekableInMemoryByteChannel(this.compressedContent),
                StandardCharsets.UTF_8.displayName());) {
            Enumeration<ZipArchiveEntry> e = z.getEntries();
            InputStream is = z.getInputStream(e.nextElement());
            return IOUtils.toString(is, StandardCharsets.UTF_8);

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return null;
    }

    /**
     * Expose compressedContent of XmlFile to save time during export and avoid
     * unnecessary decompressContent
     *
     * @return contents of the XmlFile as byte[] (whole archive)
     * @since soda4LCA 5.4.0
     */

    public byte[] getCompressedContent() {
        return compressedContent;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.common.XmlFile[id=" + this.id + "]";
    }

    public byte[] getContentHash() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            return md.digest(this.compressedContent);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

}
