package omnidrive.Api.Base;

import java.util.Date;

public interface BaseFile {

    public String getName();


    public String getPath();


    public long getSize();


    public Date getLastModified();


    public BaseUser getOwner();

}
