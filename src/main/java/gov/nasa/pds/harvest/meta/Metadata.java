package gov.nasa.pds.harvest.meta;

import gov.nasa.pds.harvest.util.FieldMapSet;


public class Metadata
{
    public String lid;
    public String vid;
    public String title;
    public String prodClass;
    
    public String fileRef;        
    
    public FieldMapSet intRefs;
    public FieldMapSet fields;

    
    public Metadata()
    {
        fields = new FieldMapSet();
    }
}
