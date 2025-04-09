package de.iai.ilcd.db.migrations;

import de.iai.ilcd.model.common.XmlFile;
import de.iai.ilcd.xml.zip.ZipArchiveBuilder;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.zip.GZIPInputStream;

/**
 * Convert each compressed entry in the "xmlfile" table from gzip to a zip
 * archive with a single entry.
 * <p>
 * That entry name is defined from a static string value
 * "ZIP_DEFAULT_ENTRY_NAME" in XmlFile class.
 *
 * @author MK
 * @see XmlFile
 * @see ZipArchiveBuilder
 * @since soda4LCA 5.4.0
 */

public class V18_0__Convertgzip2zip implements SpringJdbcMigration {
    private static final Logger LOGGER = LogManager.getLogger(V18_0__Convertgzip2zip.class);

    InputStream compressedgzip;

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

        final String sqlSelectID = "SELECT ID from xmlfile";

        jdbcTemplate.query(sqlSelectID, (rs) -> {
            Long id = rs.getLong("ID");
            try {
                selectANDupdateXML(jdbcTemplate, id);
            } catch (IOException e) {
                LOGGER.error("Unable to convert xml with id " + id);
            }
        });

    }

    private void selectANDupdateXML(JdbcTemplate jdbcTemplate, Long id) throws IOException {
        final String sqlSelectXml = "SELECT COMPRESSEDCONTENT FROM `xmlfile` WHERE ID = ? ";
        final String sqlUpdateXml = "UPDATE `xmlfile` SET COMPRESSEDCONTENT = ? WHERE ID = ? ";

        InputStream compressedContent = jdbcTemplate.queryForObject(sqlSelectXml, new Object[]{id},
                (rs, rowNum) -> new DefaultLobHandler().getBlobAsBinaryStream(rs, 1));

        String strXML = decompressGZIP(compressedContent);

        // every entry will be called "default_xml"
        byte[] zipXML = compressZip(strXML, XmlFile.ZIP_DEFAULT_ENTRY_NAME);

        jdbcTemplate.update(sqlUpdateXml, new Object[]{new SqlLobValue(zipXML), id},
                new int[]{Types.BLOB, Types.BIGINT});

    }

    private String decompressGZIP(InputStream compressedContent) {

        GZIPInputStream gzipIn;
        try {
            gzipIn = new GZIPInputStream(compressedContent);
            return new String(IOUtils.toByteArray(gzipIn), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Error decompressing", e);
        }

    }

    private byte[] compressZip(String s, String entryName) {
        // create a zip archive with a single entry

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream z = new ZipArchiveOutputStream(bs);) {
            z.putArchiveEntry(new ZipArchiveEntry(entryName));
            z.write(s.getBytes(StandardCharsets.UTF_8.name()));
            z.closeArchiveEntry();
            z.flush();
//			z.close();
        } catch (IOException e) {
            LOGGER.error("Failed to compress xml to zip");
        }

        return bs.toByteArray();
    }

}
