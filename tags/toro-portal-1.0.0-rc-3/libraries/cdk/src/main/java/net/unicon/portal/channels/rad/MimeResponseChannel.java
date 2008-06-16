/*
 * Copyright (C) 2007 Unicon, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this distribution.  It is also available here:
 * http://www.fsf.org/licensing/licenses/gpl.html
 *
 * As a special exception to the terms and conditions of version 
 * 2 of the GPL, you may redistribute this Program in connection 
 * with Free/Libre and Open Source Software ("FLOSS") applications 
 * as described in the GPL FLOSS exception.  You should have received
 * a copy of the text describing the FLOSS exception along with this
 * distribution.
 */


package net.unicon.portal.channels.rad;

import java.util.*;
import java.io.*;
import org.jasig.portal.IMimeResponse;
import org.jasig.portal.services.LogService;

/**
 * The base class for those channels that support the downloads. It implements the
 * uPortal interface IMimeResponse.
 */
public class MimeResponseChannel extends Channel
  implements IMimeResponse {
  public String getName() {
    if( m_lastScreen != null && m_lastScreen instanceof IMimeResponse)
      return ((IMimeResponse)m_lastScreen).getName();
    else
      return null;
  }

  public String getContentType() {
    if( m_lastScreen != null && m_lastScreen instanceof IMimeResponse) {
      String ct = ((IMimeResponse)m_lastScreen).getContentType();
      if( ct != null)
        return ct;
      String format = null;
      String file = ((IMimeResponse)m_lastScreen).getName();
      if( file != null) {
        int ext = file.lastIndexOf('.');
        if (ext != -1)
          format = (String)g_map.get(file.substring(ext));
      }
      return (format==null?"application/octet-stream":format);
    } else
      return null;
  }

  public InputStream getInputStream() throws IOException {
    if( m_lastScreen != null && m_lastScreen instanceof IMimeResponse)
      return ((IMimeResponse)m_lastScreen).getInputStream();
    else
      return null;
  }

  public void downloadData(OutputStream out) throws IOException {
    if( m_lastScreen != null && m_lastScreen instanceof IMimeResponse)
      ((IMimeResponse)m_lastScreen).downloadData(out);
  }

  public Map getHeaders() {
    if( m_lastScreen != null && m_lastScreen instanceof IMimeResponse)
      return ((IMimeResponse)m_lastScreen).getHeaders();
    else
      return null;
  }

  /**
   * Let the channel know that there were problems with the download
   * @param e
   */
  public void reportDownloadError(Exception e) {
    LogService.log(LogService.ERROR, getClass().getName()+"::reportDownloadError(): " + e.getMessage());
  }


  //----------------------------------------------------------------------//

  static Hashtable g_map = new java.util.Hashtable();

  static void map(String k, String v) {
    g_map.put(k, v);
  }

  static {
    map(".3dm", "x-world/x-3dmf");
    map(".3dmf", "x-world/x-3dmf");
    map(".a", "application/octet-stream");
    map(".aab", "application/x-authorware-bin");
    map(".aam", "application/x-authorware-map");
    map(".aas", "application/x-authorware-seg");
    map(".abc", "text/vnd.abc");
    map(".acgi", "text/html");
    map(".afl", "video/animaflex");
    map(".ai", "application/postscript");
    map(".aif", "audio/aiff");
    map(".aif", "audio/x-aiff");
    map(".aifc", "audio/aiff");
    map(".aifc", "audio/x-aiff");
    map(".aiff", "audio/aiff");
    map(".aiff", "audio/x-aiff");
    map(".aim", "application/x-aim");
    map(".aip", "text/x-audiosoft-intra");
    map(".ani", "application/x-navi-animation");
    map(".aos", "application/x-nokia-9000-communicator-add-on-software");
    map(".aps", "application/mime");
    map(".arc", "application/octet-stream");
    map(".arj", "application/arj");
    map(".arj", "application/octet-stream");
    map(".art", "image/x-jg");
    map(".asf", "video/x-ms-asf");
    map(".asm", "text/x-asm");
    map(".asp", "text/asp");
    map(".asx", "application/x-mplayer2");
    map(".asx", "video/x-ms-asf");
    map(".asx", "video/x-ms-asf-plugin");
    map(".au", "audio/basic");
    map(".au", "audio/x-au");
    map(".avi", "application/x-troff-msvideo");
    map(".avi", "video/avi");
    map(".avi", "video/msvideo");
    map(".avi", "video/x-msvideo");
    map(".avs", "video/avs-video");
    map(".bcpio", "application/x-bcpio");
    map(".bin", "application/mac-binary");
    map(".bin", "application/macbinary");
    map(".bin", "application/octet-stream");
    map(".bin", "application/x-binary");
    map(".bin", "application/x-macbinary");
    map(".bm", "image/bmp");
    map(".bmp", "image/bmp");
    map(".bmp", "image/x-windows-bmp");
    map(".boo", "application/book");
    map(".book", "application/book");
    map(".boz", "application/x-bzip2");
    map(".bsh", "application/x-bsh");
    map(".bz", "application/x-bzip");
    map(".bz2", "application/x-bzip2");
    map(".c", "text/plain");
    map(".c", "text/x-c");
    map(".c++", "text/plain");
    map(".cat", "application/vnd.ms-pki.seccat");
    map(".cc", "text/plain");
    map(".cc", "text/x-c");
    map(".ccad", "application/clariscad");
    map(".cco", "application/x-cocoa");
    map(".cdf", "application/cdf");
    map(".cdf", "application/x-cdf");
    map(".cdf", "application/x-netcdf");
    map(".cer", "application/pkix-cert");
    map(".cer", "application/x-x509-ca-cert");
    map(".cha", "application/x-chat");
    map(".chat", "application/x-chat");
    map(".class", "application/java");
    map(".class", "application/java-byte-code");
    map(".class", "application/x-java-class");
    map(".com", "application/octet-stream");
    map(".com", "text/plain");
    map(".conf", "text/plain");
    map(".cpio", "application/x-cpio");
    map(".cpp", "text/x-c");
    map(".cpt", "application/mac-compactpro");
    map(".cpt", "application/x-compactpro");
    map(".cpt", "application/x-cpt");
    map(".crl", "application/pkcs-crl");
    map(".crl", "application/pkix-crl");
    map(".crt", "application/pkix-cert");
    map(".crt", "application/x-x509-ca-cert");
    map(".crt", "application/x-x509-user-cert");
    map(".csh", "application/x-csh");
    map(".csh", "text/x-script.csh");
    map(".css", "application/x-pointplus");
    map(".css", "text/css");
    map(".cxx", "text/plain");
    map(".dcr", "application/x-director");
    map(".deepv", "application/x-deepv");
    map(".def", "text/plain");
    map(".der", "application/x-x509-ca-cert");
    map(".dif", "video/x-dv");
    map(".dir", "application/x-director");
    map(".dl", "video/dl");
    map(".dl", "video/x-dl");
    map(".doc", "application/msword");
    map(".dot", "application/msword");
    map(".dp", "application/commonground");
    map(".drw", "application/drafting");
    map(".dump", "application/octet-stream");
    map(".dv", "video/x-dv");
    map(".dvi", "application/x-dvi");
    map(".dwf", "drawing/x-dwf");
    map(".dwf", "model/vnd.dwf");
    map(".dwg", "application/acad");
    map(".dwg", "image/vnd.dwg");
    map(".dwg", "image/x-dwg");
    map(".dxf", "application/dxf");
    map(".dxf", "image/vnd.dwg");
    map(".dxf", "image/x-dwg");
    map(".dxr", "application/x-director");
    map(".el", "text/x-script.elisp");
    map(".elc", "application/x-bytecode.elisp");
    map(".elc", "application/x-elc");
    map(".env", "application/x-envoy");
    map(".eps", "application/postscript");
    map(".es", "application/x-esrehber");
    map(".etx", "text/x-setext");
    map(".evy", "application/envoy");
    map(".evy", "application/x-envoy");
    map(".exe", "application/octet-stream");
    map(".f", "text/plain");
    map(".f", "text/x-fortran");
    map(".f77", "text/x-fortran");
    map(".f90", "text/plain");
    map(".f90", "text/x-fortran");
    map(".fdf", "application/vnd.fdf");
    map(".fif", "application/fractals");
    map(".fif", "image/fif");
    map(".fli", "video/fli");
    map(".fli", "video/x-fli");
    map(".flo", "image/florian");
    map(".flx", "text/vnd.fmi.flexstor");
    map(".fmf", "video/x-atomic3d-feature");
    map(".for", "text/plain");
    map(".for", "text/x-fortran");
    map(".fpx", "image/vnd.fpx");
    map(".fpx", "image/vnd.net-fpx");
    map(".frl", "application/freeloader");
    map(".funk", "audio/make");
    map(".g", "text/plain");
    map(".g3", "image/g3fax");
    map(".gif", "image/gif");
    map(".gl", "video/gl");
    map(".gl", "video/x-gl");
    map(".gsd", "audio/x-gsm");
    map(".gsm", "audio/x-gsm");
    map(".gsp", "application/x-gsp");
    map(".gss", "application/x-gss");
    map(".gtar", "application/x-gtar");
    map(".gz", "application/x-compressed");
    map(".gz", "application/x-gzip");
    map(".gzip", "application/x-gzip");
    map(".gzip", "multipart/x-gzip");
    map(".h", "text/plain");
    map(".h", "text/x-h");
    map(".hdf", "application/x-hdf");
    map(".help", "application/x-helpfile");
    map(".hgl", "application/vnd.hp-HPGL");
    map(".hh", "text/plain");
    map(".hh", "text/x-h");
    map(".hlb", "text/x-script");
    map(".hlp", "application/hlp");
    map(".hlp", "application/x-helpfile");
    map(".hlp", "application/x-winhelp");
    map(".hpg", "application/vnd.hp-HPGL");
    map(".hpgl", "application/vnd.hp-HPGL");
    map(".hqx", "application/binhex");
    map(".hqx", "application/binhex4");
    map(".hqx", "application/mac-binhex");
    map(".hqx", "application/mac-binhex40");
    map(".hqx", "application/x-binhex40");
    map(".hqx", "application/x-mac-binhex40");
    map(".hta", "application/hta");
    map(".htc", "text/x-component");
    map(".htm", "text/html");
    map(".html", "text/html");
    map(".htmls", "text/html");
    map(".htt", "text/webviewhtml");
    map(".htx", "text/html");
    map(".ice", "x-conference/x-cooltalk");
    map(".ico", "image/x-icon");
    map(".idc", "text/plain");
    map(".ief", "image/ief");
    map(".iefs", "image/ief");
    map(".iges", "application/iges");
    map(".iges", "model/iges");
    map(".igs", "application/iges");
    map(".igs", "model/iges");
    map(".ima", "application/x-ima");
    map(".imap", "application/x-httpd-imap");
    map(".inf", "application/inf");
    map(".ins", "application/x-internett-signup");
    map(".ip", "application/x-ip2");
    map(".isu", "video/x-isvideo");
    map(".it", "audio/it");
    map(".iv", "application/x-inventor");
    map(".ivr", "i-world/i-vrml");
    map(".ivy", "application/x-livescreen");
    map(".jam", "audio/x-jam");
    map(".jav", "text/plain");
    map(".jav", "text/x-java-source");
    map(".java", "text/plain");
    map(".java", "text/x-java-source");
    map(".jcm", "application/x-java-commerce");
    map(".jfif", "image/jpeg");
    map(".jfif", "image/pjpeg");
    map(".jfif-tbnl", "image/jpeg");
    map(".jpe", "image/jpeg");
    map(".jpe", "image/pjpeg");
    map(".jpeg", "image/jpeg");
    map(".jpeg", "image/pjpeg");
    map(".jpg", "image/jpeg");
    map(".jpg", "image/pjpeg");
    map(".jps", "image/x-jps");
    map(".jar", "application/x-jar");
    map(".js", "application/x-javascript");
    map(".jut", "image/jutvision");
    map(".kar", "audio/midi");
    map(".kar", "music/x-karaoke");
    map(".ksh", "application/x-ksh");
    map(".ksh", "text/x-script.ksh");
    map(".la", "audio/nspaudio");
    map(".la", "audio/x-nspaudio");
    map(".lam", "audio/x-liveaudio");
    map(".latex", "application/x-latex");
    map(".lha", "application/lha");
    map(".lha", "application/octet-stream");
    map(".lha", "application/x-lha");
    map(".lhx", "application/octet-stream");
    map(".list", "text/plain");
    map(".lma", "audio/nspaudio");
    map(".lma", "audio/x-nspaudio");
    map(".log", "text/plain");
    map(".lsp", "application/x-lisp");
    map(".lsp", "text/x-script.lisp");
    map(".lst", "text/plain");
    map(".lsx", "text/x-la-asf");
    map(".ltx", "application/x-latex");
    map(".lzh", "application/octet-stream");
    map(".lzh", "application/x-lzh");
    map(".lzx", "application/lzx");
    map(".lzx", "application/octet-stream");
    map(".lzx", "application/x-lzx");
    map(".m", "text/plain");
    map(".m", "text/x-m");
    map(".m1v", "video/mpeg");
    map(".m2a", "audio/mpeg");
    map(".m2v", "video/mpeg");
    map(".m3u", "audio/x-mpequrl");
    map(".man", "application/x-troff-man");
    map(".map", "application/x-navimap");
    map(".mar", "text/plain");
    map(".mbd", "application/mbedlet");
    map(".mc$", "application/x-magic-cap-package-1.0");
    map(".mcd", "application/mcad");
    map(".mcd", "application/x-mathcad");
    map(".mcf", "image/vasa");
    map(".mcf", "text/mcf");
    map(".mcp", "application/netmc");
    map(".me", "application/x-troff-me");
    map(".mht", "message/rfc822");
    map(".mhtml", "message/rfc822");
    map(".mid", "application/x-midi");
    map(".mid", "audio/midi");
    map(".mid", "audio/x-mid");
    map(".mid", "audio/x-midi");
    map(".mid", "music/crescendo");
    map(".mid", "x-music/x-midi");
    map(".midi", "application/x-midi");
    map(".midi", "audio/midi");
    map(".midi", "audio/x-mid");
    map(".midi", "audio/x-midi");
    map(".midi", "music/crescendo");
    map(".midi", "x-music/x-midi");
    map(".mif", "application/x-frame");
    map(".mif", "application/x-mif");
    map(".mime", "message/rfc822");
    map(".mime", "www/mime");
    map(".mjf", "audio/x-vnd.AudioExplosion.MjuiceMediaFile");
    map(".mjpg", "video/x-motion-jpeg");
    map(".mm", "application/base64");
    map(".mm", "application/x-meme");
    map(".mme", "application/base64");
    map(".mod", "audio/mod");
    map(".mod", "audio/x-mod");
    map(".moov", "video/quicktime");
    map(".mov", "video/quicktime");
    map(".movie", "video/x-sgi-movie");
    map(".mp2", "audio/mpeg");
    map(".mp2", "audio/x-mpeg");
    map(".mp2", "video/mpeg");
    map(".mp2", "video/x-mpeg");
    map(".mp2", "video/x-mpeq2a");
    map(".mp3", "audio/mpeg3");
    map(".mp3", "audio/x-mpeg-3");
    map(".mp3", "video/mpeg");
    map(".mp3", "video/x-mpeg");
    map(".mpa", "audio/mpeg");
    map(".mpa", "video/mpeg");
    map(".mpc", "application/x-project");
    map(".mpe", "video/mpeg");
    map(".mpeg", "video/mpeg");
    map(".mpg", "audio/mpeg");
    map(".mpg", "video/mpeg");
    map(".mpga", "audio/mpeg");
    map(".mpp", "application/vnd.ms-project");
    map(".mpt", "application/x-project");
    map(".mpv", "application/x-project");
    map(".mpx", "application/x-project");
    map(".mrc", "application/marc");
    map(".ms", "application/x-troff-ms");
    map(".mv", "video/x-sgi-movie");
    map(".my", "audio/make");
    map(".mzz", "application/x-vnd.AudioExplosion.mzz");
    map(".nap", "image/naplps");
    map(".naplps", "image/naplps");
    map(".nc", "application/x-netcdf");
    map(".ncm", "application/vnd.nokia.configuration-message");
    map(".nif", "image/x-niff");
    map(".niff", "image/x-niff");
    map(".nix", "application/x-mix-transfer");
    map(".nsc", "application/x-conference");
    map(".nvd", "application/x-navidoc");
    map(".o", "application/octet-stream");
    map(".oda", "application/oda");
    map(".omc", "application/x-omc");
    map(".omcd", "application/x-omcdatamaker");
    map(".omcr", "application/x-omcregerator");
    map(".p", "text/x-pascal");
    map(".p10", "application/pkcs10");
    map(".p10", "application/x-pkcs10");
    map(".p12", "application/pkcs-12");
    map(".p12", "application/x-pkcs12");
    map(".p7a", "application/x-pkcs7-signature");
    map(".p7c", "application/pkcs7-mime");
    map(".p7c", "application/x-pkcs7-mime");
    map(".p7m", "application/pkcs7-mime");
    map(".p7m", "application/x-pkcs7-mime");
    map(".p7r", "application/x-pkcs7-certreqresp");
    map(".p7s", "application/pkcs7-signature");
    map(".part", "application/pro_eng");
    map(".pas", "text/pascal");
    map(".pbm", "image/x-portable-bitmap");
    map(".pcl", "application/vnd.hp-PCL");
    map(".pcl", "application/x-pcl");
    map(".pct", "image/x-pict");
    map(".pcx", "image/x-pcx");
    map(".pdb", "chemical/x-pdb");
    map(".pdf", "application/pdf");
    map(".pfunk", "audio/make");
    map(".pfunk", "audio/make.my.funk");
    map(".pgm", "image/x-portable-graymap");
    map(".pgm", "image/x-portable-greymap");
    map(".pic", "image/pict");
    map(".pict", "image/pict");
    map(".pkg", "application/x-newton-compatible-pkg");
    map(".pko", "application/vnd.ms-pki.pko");
    map(".pl", "text/plain");
    map(".pl", "text/x-script.perl");
    map(".plx", "application/x-PiXCLscript");
    map(".pm", "image/x-xpixmap");
    map(".pm", "text/x-script.perl-module");
    map(".pm4", "application/x-pagemaker");
    map(".pm5", "application/x-pagemaker");
    map(".png", "image/png");
    map(".pnm", "application/x-portable-anymap");
    map(".pnm", "image/x-portable-anymap");
    map(".pot", "application/mspowerpoint");
    map(".pot", "application/vnd.ms-powerpoint");
    map(".pov", "model/x-pov");
    map(".ppa", "application/vnd.ms-powerpoint");
    map(".ppm", "image/x-portable-pixmap");
    map(".pps", "application/mspowerpoint");
    map(".pps", "application/vnd.ms-powerpoint");
    map(".ppt", "application/mspowerpoint");
    map(".ppt", "application/powerpoint");
    map(".ppt", "application/vnd.ms-powerpoint");
    map(".ppt", "application/x-mspowerpoint");
    map(".ppz", "application/mspowerpoint");
    map(".pre", "application/x-freelance");
    map(".prt", "application/pro_eng");
    map(".ps", "application/postscript");
    map(".psd", "application/octet-stream");
    map(".pvu", "paleovu/x-pv");
    map(".pwz", "application/vnd.ms-powerpoint");
    map(".py", "text/x-script.phyton");
    map(".pyc", "applicaiton/x-bytecode.python");
    map(".qcp", "audio/vnd.qcelp");
    map(".qd3", "x-world/x-3dmf");
    map(".qd3d", "x-world/x-3dmf");
    map(".qif", "image/x-quicktime");
    map(".qt", "video/quicktime");
    map(".qtc", "video/x-qtc");
    map(".qti", "image/x-quicktime");
    map(".qtif", "image/x-quicktime");
    map(".ra", "audio/x-pn-realaudio");
    map(".ra", "audio/x-pn-realaudio-plugin");
    map(".ra", "audio/x-realaudio");
    map(".ram", "audio/x-pn-realaudio");
    map(".ras", "application/x-cmu-raster");
    map(".ras", "image/cmu-raster");
    map(".ras", "image/x-cmu-raster");
    map(".rast", "image/cmu-raster");
    map(".rexx", "text/x-script.rexx");
    map(".rf", "image/vnd.rn-realflash");
    map(".rgb", "image/x-rgb");
    map(".rm", "application/vnd.rn-realmedia");
    map(".rm", "audio/x-pn-realaudio");
    map(".rmi", "audio/mid");
    map(".rmm", "audio/x-pn-realaudio");
    map(".rmp", "audio/x-pn-realaudio");
    map(".rmp", "audio/x-pn-realaudio-plugin");
    map(".rng", "application/ringing-tones");
    map(".rng", "application/vnd.nokia.ringing-tone");
    map(".rnx", "application/vnd.rn-realplayer");
    map(".roff", "application/x-troff");
    map(".rp", "image/vnd.rn-realpix");
    map(".rpm", "audio/x-pn-realaudio-plugin");
    map(".rt", "text/richtext");
    map(".rt", "text/vnd.rn-realtext");
    map(".rtf", "application/rtf");
    map(".rtf", "application/x-rtf");
    map(".rtf", "text/richtext");
    map(".rtx", "application/rtf");
    map(".rtx", "text/richtext");
    map(".rv", "video/vnd.rn-realvideo");
    map(".s", "text/x-asm");
    map(".s3m", "audio/s3m");
    map(".saveme", "application/octet-stream");
    map(".sbk", "application/x-tbook");
    map(".scm", "application/x-lotusscreencam");
    map(".scm", "text/x-script.guile");
    map(".scm", "text/x-script.scheme");
    map(".scm", "video/x-scm");
    map(".sdml", "text/plain");
    map(".sdp", "application/sdp");
    map(".sdp", "application/x-sdp");
    map(".sdr", "application/sounder");
    map(".sea", "application/sea");
    map(".sea", "application/x-sea");
    map(".set", "application/set");
    map(".sgm", "text/sgml");
    map(".sgm", "text/x-sgml");
    map(".sgml", "text/sgml");
    map(".sgml", "text/x-sgml");
    map(".sh", "application/x-bsh");
    map(".sh", "application/x-sh");
    map(".sh", "application/x-shar");
    map(".sh", "text/x-script.sh");
    map(".shar", "application/x-bsh");
    map(".shar", "application/x-shar");
    map(".shtml", "text/html");
    map(".shtml", "text/x-server-parsed-html");
    map(".sid", "audio/x-psid");
    map(".sit", "application/x-sit");
    map(".sit", "application/x-stuffit");
    map(".skd", "application/x-koan");
    map(".skm", "application/x-koan");
    map(".skp", "application/x-koan");
    map(".skt", "application/x-koan");
    map(".sl", "application/x-seelogo");
    map(".smi", "application/smil");
    map(".smil", "application/smil");
    map(".snd", "audio/basic");
    map(".snd", "audio/x-adpcm");
    map(".sol", "application/solids");
    map(".spc", "application/x-pkcs7-certificates");
    map(".spc", "text/x-speech");
    map(".spl", "application/futuresplash");
    map(".spr", "application/x-sprite");
    map(".sprite", "application/x-sprite");
    map(".src", "application/x-wais-source");
    map(".ssi", "text/x-server-parsed-html");
    map(".ssm", "application/streamingmedia");
    map(".sst", "application/vnd.ms-pki.certstore");
    map(".step", "application/step");
    map(".stl", "application/sla");
    map(".stl", "application/vnd.ms-pki.stl");
    map(".stl", "application/x-navistyle");
    map(".stp", "application/step");
    map(".sv4cpio", "application/x-sv4cpio");
    map(".sv4crc", "application/x-sv4crc");
    map(".svf", "image/vnd.dwg");
    map(".svf", "image/x-dwg");
    map(".svr", "application/x-world");
    map(".svr", "x-world/x-svr");
    map(".swf", "application/x-shockwave-flash");
    map(".t", "application/x-troff");
    map(".talk", "text/x-speech");
    map(".tar", "application/x-tar");
    map(".tbk", "application/toolbook");
    map(".tbk", "application/x-tbook");
    map(".tcl", "application/x-tcl");
    map(".tcl", "text/x-script.tcl");
    map(".tcsh", "text/x-script.tcsh");
    map(".tex", "application/x-tex");
    map(".texi", "application/x-texinfo");
    map(".texinfo", "application/x-texinfo");
    map(".text", "application/plain");
    map(".text", "text/plain");
    map(".tgz", "application/gnutar");
    map(".tgz", "application/x-compressed");
    map(".tif", "image/tiff");
    map(".tif", "image/x-tiff");
    map(".tiff", "image/tiff");
    map(".tiff", "image/x-tiff");
    map(".tr", "application/x-troff");
    map(".tsi", "audio/tsp-audio");
    map(".tsp", "application/dsptype");
    map(".tsp", "audio/tsplayer");
    map(".tsv", "text/tab-separated-values");
    map(".turbot", "image/florian");
    map(".txt", "text/plain");
    map(".uil", "text/x-uil");
    map(".uni", "text/uri-list");
    map(".unis", "text/uri-list");
    map(".unv", "application/i-deas");
    map(".uri", "text/uri-list");
    map(".uris", "text/uri-list");
    map(".ustar", "application/x-ustar");
    map(".ustar", "multipart/x-ustar");
    map(".uu", "application/octet-stream");
    map(".uu", "text/x-uuencode");
    map(".uue", "text/x-uuencode");
    map(".vcd", "application/x-cdlink");
    map(".vcs", "text/x-vCalendar");
    map(".vda", "application/vda");
    map(".vdo", "video/vdo");
    map(".vew", "application/groupwise");
    map(".viv", "video/vivo");
    map(".viv", "video/vnd.vivo");
    map(".vivo", "video/vivo");
    map(".vivo", "video/vnd.vivo");
    map(".vmd", "application/vocaltec-media-desc");
    map(".vmf", "application/vocaltec-media-file");
    map(".voc", "audio/voc");
    map(".voc", "audio/x-voc");
    map(".vos", "video/vosaic");
    map(".vox", "audio/voxware");
    map(".vqe", "audio/x-twinvq-plugin");
    map(".vqf", "audio/x-twinvq");
    map(".vql", "audio/x-twinvq-plugin");
    map(".vrml", "application/x-vrml");
    map(".vrml", "model/vrml");
    map(".vrml", "x-world/x-vrml");
    map(".vrt", "x-world/x-vrt");
    map(".vsd", "application/x-visio");
    map(".vst", "application/x-visio");
    map(".vsw", "application/x-visio");
    map(".w60", "application/wordperfect6.0");
    map(".w61", "application/wordperfect6.1");
    map(".w6w", "application/msword");
    map(".wav", "audio/wav");
    map(".wav", "audio/x-wav");
    map(".wb1", "application/x-qpro");
    map(".wbmp", "image/vnd.wap.wbmp");
    map(".web", "application/vnd.xara");
    map(".wiz", "application/msword");
    map(".wk1", "application/x-123");
    map(".wmf", "windows/metafile");
    map(".wml", "text/vnd.wap.wml");
    map(".wmlc", "application/vnd.wap.wmlc");
    map(".wmls", "text/vnd.wap.wmlscript");
    map(".wmlsc", "application/vnd.wap.wmlscriptc");
    map(".word", "application/msword");
    map(".wp", "application/wordperfect");
    map(".wp5", "application/wordperfect");
    map(".wp5", "application/wordperfect6.0");
    map(".wp6", "application/wordperfect");
    map(".wpd", "application/wordperfect");
    map(".wpd", "application/x-wpwin");
    map(".wq1", "application/x-lotus");
    map(".wri", "application/mswrite");
    map(".wri", "application/x-wri");
    map(".wrl", "application/x-world");
    map(".wrl", "model/vrml");
    map(".wrl", "x-world/x-vrml");
    map(".wrz", "model/vrml");
    map(".wrz", "x-world/x-vrml");
    map(".wsc", "text/scriplet");
    map(".wsrc", "application/x-wais-source");
    map(".wtk", "application/x-wintalk");
    map(".xbm", "image/x-xbitmap");
    map(".xbm", "image/x-xbm");
    map(".xbm", "image/xbm");
    map(".xdr", "video/x-amt-demorun");
    map(".xgz", "xgl/drawing");
    map(".xif", "image/vnd.xiff");
    map(".xl", "application/excel");
    map(".xla", "application/excel");
    map(".xla", "application/x-excel");
    map(".xla", "application/x-msexcel");
    map(".xlb", "application/excel");
    map(".xlb", "application/vnd.ms-excel");
    map(".xlb", "application/x-excel");
    map(".xlc", "application/excel");
    map(".xlc", "application/vnd.ms-excel");
    map(".xlc", "application/x-excel");
    map(".xld", "application/excel");
    map(".xld", "application/x-excel");
    map(".xlk", "application/excel");
    map(".xlk", "application/x-excel");
    map(".xll", "application/excel");
    map(".xll", "application/vnd.ms-excel");
    map(".xll", "application/x-excel");
    map(".xlm", "application/excel");
    map(".xlm", "application/vnd.ms-excel");
    map(".xlm", "application/x-excel");
    map(".xls", "application/excel");
    map(".xls", "application/vnd.ms-excel");
    map(".xls", "application/x-excel");
    map(".xls", "application/x-msexcel");
    map(".xlt", "application/excel");
    map(".xlt", "application/x-excel");
    map(".xlv", "application/excel");
    map(".xlv", "application/x-excel");
    map(".xlw", "application/excel");
    map(".xlw", "application/vnd.ms-excel");
    map(".xlw", "application/x-excel");
    map(".xlw", "application/x-msexcel");
    map(".xm", "audio/xm");
    map(".xml", "application/xml");
    map(".xml", "text/xml");
    map(".xmz", "xgl/movie");
    map(".xpix", "application/x-vnd.ls-xpix");
    map(".xpm", "image/x-xpixmap");
    map(".xpm", "image/xpm");
    map(".x-png", "image/png");
    map(".xsr", "video/x-amt-showrun");
    map(".xwd", "image/x-xwd");
    map(".xwd", "image/x-xwindowdump");
    map(".xyz", "chemical/x-pdb");
    map(".z", "application/x-compress");
    map(".z", "application/x-compressed");
    map(".zip", "application/x-compressed");
    map(".zip", "application/x-zip-compressed");
    map(".zip", "application/zip");
    map(".zip", "multipart/x-zip");
    map(".zoo", "application/octet-stream");
    map(".zsh", "text/x-script.zsh");
  }
}
