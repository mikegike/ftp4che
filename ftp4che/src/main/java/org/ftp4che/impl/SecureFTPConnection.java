/**                                                                         
 *  This file is part of ftp4che.                                            
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ftp4che.impl;

import java.io.IOException;
import javax.net.ssl.SSLException;
import org.apache.log4j.Logger;
import org.ftp4che.FTPConnection;
import org.ftp4che.commands.Command;
import org.ftp4che.event.FTPEvent;
import org.ftp4che.exception.AuthenticationNotSupportedException;
import org.ftp4che.exception.FtpIOException;
import org.ftp4che.exception.FtpWorkflowException;
import org.ftp4che.exception.NotConnectedException;
import org.ftp4che.io.ReplyWorker;
import org.ftp4che.io.SocketProvider;
import org.ftp4che.reply.Reply;
import org.ftp4che.reply.ReplyCode;

public class SecureFTPConnection extends FTPConnection {
    Logger log = Logger.getLogger(SecureFTPConnection.class.getName());

    @Override
    public void connect() throws NotConnectedException, IOException,
            AuthenticationNotSupportedException, FtpIOException,
            FtpWorkflowException {
        
        setConnectionStatusLock(ConnectionStatus.CSL_INDIRECT_CALL);
        
        socketProvider = new SocketProvider();
        try {
            socketProvider.socket().setSoTimeout(getTimeout());
            socketProvider.connect(getAddress(), getProxy(),
                    getDownloadBandwidth(), getUploadBandwidth());
            log.debug("connected to:" + getAddress().getHostName() + ":"
                    + getAddress().getPort());       
            socketProvider.socket().setKeepAlive(true);
        } catch (IOException ioe) {
            String error = "Error connection to:" + getAddress().getHostName() + ":"
            + getAddress().getPort();
            log.error(error, ioe);
            throw new NotConnectedException(error);
        }
        // Till here the connection is not encrypted!!
        if (this.getConnectionType() == FTPConnection.IMPLICIT_SSL_FTP_CONNECTION
                || this.getConnectionType() == FTPConnection.IMPLICIT_TLS_FTP_CONNECTION
                || this.getConnectionType() == FTPConnection.IMPLICIT_SSL_WITH_CRYPTED_DATA_FTP_CONNECTION
                || this.getConnectionType() == FTPConnection.IMPLICIT_TLS_WITH_CRYPTED_DATA_FTP_CONNECTION) {
            negotiateAndLogin(null);
        } else {
        	Reply connectReply = ReplyWorker.readReply(this.socketProvider);
            if (connectReply != null)
                this.fireReplyMessageArrived(new FTPEvent(this, this.getConnectionStatus(), connectReply));
            connectReply.dumpReply();
            
            negotiateAndLogin(getAuthString());
        }
        checkFeatures();

        this.setConnectionStatus(ConnectionStatus.CONNECTED);
        this.setConnectionStatus(ConnectionStatus.IDLE);
     
        checkSystem();
        
        setConnectionStatusLock(ConnectionStatus.CSL_DIRECT_CALL);
    }

    private void negotiateAndLogin(String authCommand) throws IOException,
            AuthenticationNotSupportedException, FtpWorkflowException,
            FtpIOException {
        Reply reply = null;
        if (authCommand != null) // null means implicit SSL
        {
            reply = sendCommand(new Command(authCommand));
            reply.dumpReply();
        }
        if (authCommand == null || ReplyCode.isPositiveCompletionReply(reply)) {
            try {
                socketProvider.setSSLMode(getConnectionType());
                socketProvider.negotiateWithException(this.getTrustManagers(),this.getKeyManagers());
                if (authCommand == null) {
                    // We are in implicit mode and must read the initial reply
                	Reply connectReply = ReplyWorker.readReply(this.socketProvider);
                    if (connectReply != null)
                        this.fireReplyMessageArrived(new FTPEvent(this, this.getConnectionStatus(), connectReply));
                    connectReply.dumpReply();
                }
            } catch (Exception e) {
                if(e instanceof SSLException) {
                  throw (SSLException)e;
                }
                log.error(e, e);
            }
            reply = sendCommand(new Command(Command.USER, getUser()));
            reply.dumpReply();
            reply.validate();
            if (getPassword() != null && getPassword().length() > 0) {
                reply = sendCommand(new Command(Command.PASS, getPassword()));
                reply.dumpReply();
                reply.validate();
            }
            if (getAccount() != null && getAccount().length() > 0) {
                reply = sendCommand(new Command(Command.ACCT, getAccount()));
                reply.dumpReply();
                reply.validate();
            }
        } else {
            throw new AuthenticationNotSupportedException(authCommand
                    + " not supported by server");
        }
    }

    private String getAuthString() {
        switch (this.getConnectionType()) {
        case FTPConnection.AUTH_SSL_FTP_CONNECTION:
            return "AUTH SSL";
        case FTPConnection.AUTH_TLS_FTP_CONNECTION:
            return "AUTH TLS";
        }

        return "SHOULD NOT HAPPEN";
    }
}
