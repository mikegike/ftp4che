/**
 * Created on 11.06.2005
 * @author arnold, kurt
 */
package org.ftp4che.util;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import org.apache.log4j.Logger;
import org.ftp4che.FTPConnection;





public class SocketProvider {

    private SSLSupport supporter;
	private int sslMode = FTPConnection.FTP_CONNECTION; 

    SocketChannel socketChan = null;
	Logger log = Logger.getLogger(SocketProvider.class.getName());
    
	ByteBuffer applicationIn, applicationOut,networkIn,networkOut;
    boolean isControllConnection = true;
    
	public SocketProvider() throws IOException {
		socketChan = SocketChannel.open();
	}
    
    public SocketProvider(boolean isControllConnection) throws IOException {
        this();
        setControllConnection(isControllConnection);
    }
    
	public SocketProvider( SocketChannel socketChan ) {
		this.socketChan = socketChan;
	}
    
    public SocketProvider(SocketChannel socketChan, boolean isControllConnection )
    {
        this(socketChan);
        setControllConnection(isControllConnection);
    }
    
	public boolean connect( SocketAddress remote ) throws IOException {
		return socketChan.connect(remote);
	}
    
    
	
	public boolean finishConnect() throws IOException {
		return socketChan.finishConnect();
	}
	
	public Socket socket() {
		return socketChan.socket();
	}
	
	
	public boolean isConnected() {
		return socketChan.isConnected();
	}
    
    public boolean needsCrypt()
    {
        return   ((this.sslMode == FTPConnection.AUTH_SSL_FTP_CONNECTION || 
                this.sslMode == FTPConnection.AUTH_TLS_FTP_CONNECTION) && !isControllConnection()) ||
                this.sslMode != FTPConnection.FTP_CONNECTION && isControllConnection();
    }
	
	public void close() throws IOException {

        if (needsCrypt())
        {
            if ( supporter != null )
               ;
            	//supporter;
        }        
        socketChan.close();
	}
	
	public SelectableChannel configureBlocking( boolean blockingState ) throws IOException {
		return socketChan.configureBlocking(blockingState);
	}
	

	public int write(ByteBuffer src) throws IOException 
	{
        if (needsCrypt())
        {
        	//return supporter.write(src);
        	throw new IOException("SSL NOT IMPLEMENTED YET");
        }
		return socketChan.write(src);
	}
	
	public int read( ByteBuffer dst ) throws IOException {
        if (needsCrypt())
        {
           // return supporter.read(dst);
        	throw new IOException("SSL NOT IMPLEMENTED YET");
        }
		return socketChan.read(dst);
	}
	
	
	public String toString() {
		return socketChan.socket().getInetAddress().getHostAddress() + ":" + socketChan.socket().getPort();
	}

    /**
     * @return Returns the sslMode.
     */
    public int getSSLMode() {
        return sslMode;
    }

    /**
     * @param sslMode The sslMode to set.
     */
    public void setSSLMode(int sslMode) {
        this.sslMode = sslMode;
    }
        
    public boolean isInboundDone() {
    	//TODO: NEEDED BY ANYONE ???
        return true;
    }
    
    /**
     * @return Returns the isControllConnection.
     */
    public boolean isControllConnection() {
        return isControllConnection;
    }

    /**
     * @param isControllConnection The isControllConnection to set.
     */
    public void setControllConnection(boolean isControllConnection) {
        this.isControllConnection = isControllConnection;
    }
    
    public void negotiate() {
        try {
        	//TODO: Do handshake here 
        	/*   supporter = new SSLSupport(socketChan, getSSLMode());
            supporter.initEngineAndBuffers();
            supporter.handshake();
            */
        	
        }catch (Exception e) {
            log.fatal(e,e);
        }
    }
}
