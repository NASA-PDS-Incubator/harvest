package gov.nasa.pds.harvest.crawler;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.meta.BasicMetadataExtractor;
import gov.nasa.pds.harvest.meta.FileMetadataExtractor;
import gov.nasa.pds.harvest.meta.InternalReferenceExtractor;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.out.DocWriter;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class MetadataProcessor
{
    private Logger LOG;

    private Configuration config;
    private DocWriter writer;
    
    private BasicMetadataExtractor basicExtractor;
    private InternalReferenceExtractor refExtractor;
    private AutogenExtractor autogenExtractor;
    private FileMetadataExtractor fileDataExtractor;
    private XPathExtractor xpathExtractor;
    
    
    public MetadataProcessor(DocWriter writer, Configuration config) throws Exception
    {
        LOG = LogManager.getLogger(getClass());
        
        this.writer = writer;
        
        basicExtractor = new BasicMetadataExtractor();
        refExtractor = new InternalReferenceExtractor(config.internalRefs);
        autogenExtractor = new AutogenExtractor(config.autogen);
        fileDataExtractor = new FileMetadataExtractor(config);
        xpathExtractor = new XPathExtractor();
        
        this.config = config;
    }

    
    public void process(File file, Counter counter) throws Exception
    {
        LOG.info("Processing file " + file.toURI().getPath());

        // Parse XML, ignore namespaces. 
        // Basic and XPath extractor are much easier to use without namespaces.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        Document doc = XmlDomUtils.readXml(dbf, file);
        String rootElement = doc.getDocumentElement().getNodeName();
        
        // Extract basic metadata
        Metadata meta = basicExtractor.extract(doc);
        
        // Internal references
        refExtractor.addRefs(meta.intRefs, doc);
        
        // Extract fields by XPath
        xpathExtractor.extract(doc, meta.fields);

        // Extract fields autogenerated from data dictionary
        if(config.autogen != null)
        {
            autogenExtractor.extract(file, meta.fields);
        }

        // Extract file data
        fileDataExtractor.extract(file, meta);
        
        writer.write(meta);
        
        counter.prodCounters.inc(rootElement);
    }
    
    
    public void close() throws Exception
    {
        writer.close();
    }
    
    
}
