/**                                                                         *
*  This file is part of ftp4che.                                            *
*                                                                           *
*  This library is free software; you can redistribute it and/or modify it  *
*  under the terms of the GNU General Public License as published    		*
*  by the Free Software Foundation; either version 2 of the License, or     *
*  (at your option) any later version.                                      *
*                                                                           *
*  This library is distributed in the hope that it will be useful, but      *
*  WITHOUT ANY WARRANTY; without even the implied warranty of               *
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
*  General Public License for more details.                          		*
*                                                                           *
*  You should have received a copy of the GNU General Public		        *
*  License along with this library; if not, write to the Free Software      *
*  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
*                                                                           *
*****************************************************************************/
package org.ftp4che.util;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.ftp4che.exception.UnkownReplyStateException;
import org.ftp4che.reply.Reply;

public class ReplyFormatter {
	public static Logger log = Logger.getLogger(ReplyFormatter.class.getName());
	
    public static String parsePWDReply(Reply pwdReply) throws UnkownReplyStateException
    {
        List lines = pwdReply.getLines();
        if(lines.size() != 1)
            throw new UnkownReplyStateException("PWD Reply has to have a size of 1 entry but it has: " + lines.size());
        String line = (String)lines.get(0);
        //LINE: 257 "/" is current directory.
        return line.substring(line.indexOf('"') + 1,line.lastIndexOf('"'));     
    }

    public static List parseListReply(Reply listReply) 
    {
        List lines = listReply.getLines();
        List parsedLines = new ArrayList(lines.size());
        for(Iterator it=lines.iterator();it.hasNext();)
        {
         
            parsedLines.add(FTPFile.parseLine((String)it.next()));
        }
        return parsedLines;
    }
    
    public static InetSocketAddress parsePASVCommand(Reply pasvReply) throws UnkownReplyStateException
    {
       List lines = pasvReply.getLines();
        if(lines.size() != 1)
            throw new UnkownReplyStateException("PASV Reply has to have a size of 1 entry but it has: " + lines.size());
        String line = (String)lines.get(0);
        line = line.substring(line.indexOf('(')+1,line.lastIndexOf(')'));
        String[] host = line.split(",");
        log.debug("Parsed host:" + host[0] + "." + host[1] + "." + host[2] + "." + host[3] + " port: " + ((Integer.parseInt(host[4]) << 8) + Integer.parseInt(host[5])));
        return new InetSocketAddress(host[0] + "." + host[1] + "." + host[2] + "." + host[3],(Integer.parseInt(host[4]) << 8) + Integer.parseInt(host[5]));
    }
}