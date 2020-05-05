package gov.nasa.pds.harvest.util.date;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PDSDateConverter
{
    public static final String DEFAULT_STARTTIME = "1965-01-01T00:00:00.000Z";
    public static final String DEFAULT_STOPTIME = "3000-01-01T00:00:00.000Z";

    private static final Logger LOG = LogManager.getLogger(PDSDateConverter.class);
       
    private CompactDateTimeConverter compactDateTimeConverter;
    private DoyDateTimeConverter doyDateTimeConverter;
    private IsoDateTimeConverter isoDateTimeConverter;
    private LocalDateConverter localDateConverter;

    
    public PDSDateConverter()
    {
        compactDateTimeConverter = new CompactDateTimeConverter();
        doyDateTimeConverter = new DoyDateTimeConverter();
        isoDateTimeConverter = new IsoDateTimeConverter();
        localDateConverter = new LocalDateConverter();
    }


    public String toSolrDateString(String fieldName, String value)
    {
        if(value == null) return null;
        
        if(value.isEmpty() 
                || value.equalsIgnoreCase("N/A") 
                || value.equalsIgnoreCase("UNK") 
                || value.equalsIgnoreCase("NULL")
                || value.equalsIgnoreCase("UNKNOWN"))
        {
            return getDefaultValue(fieldName);
        }

        // DateTime
        if(value.length() > 10)
        {
            String newValue = convertIsoDateTime(value);
            if(newValue != null) return newValue;

            newValue = convertCompactDateTime(value);
            if(newValue != null) return newValue;

            newValue = convertDoyTime(value);
            if(newValue != null) return newValue;
            
            LOG.warn("Could not convert date " + value);
        }
        // Date only
        else
        {
            String newValue = convertDate(value);
            if(newValue != null) return newValue;
            
            LOG.warn("Could not convert date " + value);
        }
        
        return null;
    }


    private String convertIsoDateTime(String value)
    {
        try
        {
            Instant inst = isoDateTimeConverter.toInstant(value);
            return toInstantString(inst);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    
    private String convertCompactDateTime(String value)
    {
        try
        {
            Instant inst = compactDateTimeConverter.toInstant(value);
            return toInstantString(inst);
        }
        catch(Exception ex)
        {
            return null;
        }
    }


    private String convertDoyTime(String value)
    {
        try
        {
            Instant inst = doyDateTimeConverter.toInstant(value);
            return toInstantString(inst);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    
    private String convertDate(String value)
    {
        try
        {
            Instant inst = localDateConverter.toInstant(value);
            return toInstantString(inst);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    
    private static String toInstantString(Instant inst)
    {
        return (inst == null) ? null : DateTimeFormatter.ISO_INSTANT.format(inst);
    }

    
    public static String getDefaultValue(String fieldName)
    {
        if(fieldName == null) return null;
        
        if(fieldName.toLowerCase().contains("start"))
        {
            return DEFAULT_STARTTIME;
        }
        else
        {
            return DEFAULT_STOPTIME;
        }
    }

}
