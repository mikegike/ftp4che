package org.ftp4che.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ftp4che.reply.Reply;
import org.ftp4che.util.FTPFile;
import org.ftp4che.util.ReplyWorker;

public class StoreCommand extends DataConnectionCommand {
    
    FTPFile toFile;
    File fromFile;
    //TODO: throw Exception if fromFile not Exists
    
    
    public StoreCommand(String command, FTPFile toFile)
    {
       super(command, new String[] {toFile.getName()} );
       setToFile( toFile );
    }
    
    public StoreCommand(String command, File fromFile, FTPFile toFile)
    {
       super(command, new String[] {toFile.getName()} );
       setToFile( toFile );
       setFromFile( fromFile );
       
    }
    
    public Reply fetchDataConnectionReply() throws FileNotFoundException,IOException{
    	  ReplyWorker worker = new ReplyWorker(getDataSocket(),this);
          worker.start();
          while(worker.getStatus() == ReplyWorker.UNKNOWN)
          {
          	try
          	{
          		Thread.sleep(20);
          	}catch (InterruptedException ie) {}
          }
          if(worker.getStatus() == ReplyWorker.FINISHED)
          {
          	return worker.getReply();
          }
          else
          {
              if(worker.getCaughtException() instanceof FileNotFoundException)
                  throw (FileNotFoundException)worker.getCaughtException();
              else
                  throw (IOException)worker.getCaughtException();        
          }
    }
    
    /**
     * @return Returns the file.
     */
    public FTPFile getToFile() {
        return toFile;
    }
    
    /**
     * @param file The file to set.
     */
    public void setToFile(FTPFile file) {
        this.toFile = file;
    }

    /**
     * @return Returns the fromFile.
     */
    public File getFromFile() {
        return fromFile;
    }

    /**
     * @param fromFile The fromFile to set.
     */
    public void setFromFile(File fromFile) {
        this.fromFile = fromFile;
    }
}
