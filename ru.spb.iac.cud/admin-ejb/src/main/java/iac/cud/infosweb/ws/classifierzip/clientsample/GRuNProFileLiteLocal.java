package iac.cud.infosweb.ws.classifierzip.clientsample;

import java.sql.*;
import java.io.*;
import java.util.Date;
import java.util.*;
import java.text.SimpleDateFormat;
import java.nio.channels.FileChannel;

import javax.ejb.Local;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;

/**
 * Программа, обеспечивающая загрузку данных 
 * входной посылки в соответствующей схеме БД
 * @author bubnov
 *
 */
@Local
public interface GRuNProFileLiteLocal {
	
	public int process(/*final String tableName, final List <String> fileList, final String dirName*/);
}
