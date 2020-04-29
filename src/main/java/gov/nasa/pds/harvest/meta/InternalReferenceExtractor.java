package gov.nasa.pds.harvest.meta;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.InternalRefCfg;
import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


public class InternalReferenceExtractor
{
//////////////////////////////////////////////////////////
    
    private static class LidRef
    {
        public String lid;
        public String lidvid;
        public String type;
    }
    
//////////////////////////////////////////////////////////

    private InternalRefCfg cfg;    
    private XPathExpression xIntRef;
    
    
    public InternalReferenceExtractor(InternalRefCfg cfg) throws Exception
    {
        this.cfg = cfg;
        XPathFactory xpf = XPathFactory.newInstance();
        xIntRef = XPathUtils.compileXPath(xpf, "//Internal_Reference");
    }
    
    
    public FieldMap extract(Document doc) throws Exception
    {
        if(cfg == null) return null;
        
        NodeList nodes = XPathUtils.getNodeList(doc, xIntRef);        
        if(nodes == null || nodes.getLength() == 0) return null;

        FieldMap fmap = new FieldMap();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            LidRef ref = createLidRef(nodes.item(i));
            if(ref != null)
            {
                addRef(fmap, ref);
            }
        }

        return fmap;
    }

    
    private void addRef(FieldMap fmap, LidRef ref)
    {
        if(ref.type == null) return;
        
        String[] tokens = ref.type.split("_");
        
        String name = tokens[tokens.length-1];
        
        if(tokens.length > 1) 
        { 
            if(name.equals("host") && tokens[tokens.length-2].equals("instrument"))
            {
                name = "instrument_host";
            }
            else if(name.equals("kernel") && tokens[tokens.length-2].equals("spice"))
            {
                name = "spice_kernel";
            }            
        }
        
        if(ref.lid != null)
        {
            String key = cfg.prefix + "lid_" + name;
            fmap.addValue(key, ref.lid);
        }

        if(ref.lidvid != null)
        {
            String key = cfg.prefix + "lidvid_" + name;
            fmap.addValue(key, ref.lidvid);
        }
    }
    
    
    private LidRef createLidRef(Node root)
    {
        LidRef ref = new LidRef();
    
        NodeList nodes = root.getChildNodes();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            
            if(nodeName.equals("lid_reference"))
            {
                ref.lid = node.getTextContent().trim();
            }
            else if(nodeName.equals("reference_type"))
            {
                ref.type = node.getTextContent().trim(); 
            }
            else if(nodeName.equals("lidvid_reference"))
            {
                ref.lidvid = node.getTextContent().trim();
            }
        }
        
        if(cfg.lidvidConvert && ref.lidvid != null && ref.lid == null)
        {
            int idx = ref.lidvid.indexOf("::");
            if(idx > 0)
            {
                ref.lid = ref.lidvid.substring(0, idx);
            }
        }
        
        if(!cfg.lidvidKeep) 
        {
            ref.lidvid = null;
        }
        
        return ref;
    }
}
