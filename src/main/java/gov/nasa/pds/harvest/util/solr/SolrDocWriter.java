package gov.nasa.pds.harvest.util.solr;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Set;

import gov.nasa.pds.harvest.meta.FileData;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


public class SolrDocWriter
{
    private Writer writer;
    
    public SolrDocWriter(File outDir) throws Exception
    {
        File file = new File(outDir, "solr-docs.xml");        
        writer = new FileWriter(file);
        
        writer.append("<add>\n");
    }

    
    public void close() throws Exception
    {
        writer.append("</add>\n");
        writer.close();
    }

    
    public void write(FileData fileData, Metadata meta) throws Exception
    {
        writer.append("<doc>\n");

        // Basic info
        String lidvid = meta.lid + "::" + meta.vid;        
        SolrDocUtils.writeField(writer, "lid", meta.lid);
        SolrDocUtils.writeField(writer, "vid", meta.vid);
        SolrDocUtils.writeField(writer, "lidvid", lidvid);
        SolrDocUtils.writeField(writer, "title", meta.title);
        SolrDocUtils.writeField(writer, "product_class", meta.prodClass);
        
        // File Info
        SolrDocUtils.writeField(writer, "_file_ref", meta.fileRef);

        if(fileData != null)
        {
            SolrDocUtils.writeField(writer, "_file_name", fileData.name);
            SolrDocUtils.writeField(writer, "_file_type", fileData.mimeType);
            SolrDocUtils.writeField(writer, "_file_size", fileData.size);

            // File BLOB
            SolrDocUtils.writeField(writer, "_file_blob", fileData.blobBase64);
            SolrDocUtils.writeField(writer, "_file_md5", fileData.md5Base64);
        }

        // Transaction ID
        SolrDocUtils.writeField(writer, "_package_id", PackageIdGenerator.getInstance().getPackageId());
        
        // References
        write(meta.intRefs);
        
        // Other Fields
        write(meta.fields);
        
        writer.append("</doc>\n");
    }
 
    
    private void write(FieldMap fmap) throws Exception
    {
        if(fmap == null || fmap.isEmpty()) return;
        
        for(String key: fmap.getNames())
        {
            Set<String> values = fmap.getValues(key);
            for(String value: values)
            {
                SolrDocUtils.writeField(writer, key, value);
            }
        }
    }
}
