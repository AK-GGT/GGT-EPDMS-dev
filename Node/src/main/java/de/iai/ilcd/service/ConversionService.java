package de.iai.ilcd.service;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.rest.ConvertResource;
import de.iai.ilcd.webgui.controller.admin.UploadedFileInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.PathParam;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>
 * Sequence diagrams roughly illustrate how conversion procedure work:
 * FORMATTEROFF
 * </p>
 *
 *
 * <svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="681" height="523"><defs/><g><g/><g/><g/><g/><g/><g/><g/><g/><g/><g/><g><rect fill="white" stroke="none" x="0" y="0" width="681" height="523"/></g><g><text fill="black" stroke="none" font-family="sans-serif" font-size="16.5pt" font-style="normal" font-weight="normal" text-decoration="normal" x="272.6552868771289" y="22.6179738" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">XML ⇨ XLSX</text></g><g/><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 230.0083049399609 88.511670804 L 230.0083049399609 523.3543270105524" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="11.598960923076923,5.0262164"/><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 581.3770937571484 88.511670804 L 581.3770937571484 523.3543270105524" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="11.598960923076923,5.0262164"/></g><g><path fill="none" stroke="none"/><g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 179.42619931772654 47.799317964000004 L 280.5904105621953 47.799317964000004 L 280.5904105621953 88.511670804 L 179.42619931772654 88.511670804 L 179.42619931772654 47.799317964000004 Z" stroke-miterlimit="10" stroke-width="2.4125838720000004" stroke-dasharray=""/></g><g><g/><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="195.78653369972653" y="73.43302160400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">soda4LCA</text></g><path fill="none" stroke="none"/><g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 503.9263891969257 47.799317964000004 L 658.827798317371 47.799317964000004 L 658.827798317371 88.511670804 L 503.9263891969257 88.511670804 L 503.9263891969257 47.799317964000004 Z" stroke-miterlimit="10" stroke-width="2.4125838720000004" stroke-dasharray=""/></g><g><g/><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="520.2867235789257" y="73.43302160400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">conversion-service</text></g></g><g><g><g><rect fill="white" stroke="none" x="247.8513731599609" y="118.668969204" width="315.6826523771875" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="250.1131705399609" y="132.239753484" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">[POST] /convert/ URL for locally hosted process</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 230.0083049399609 138.27121316400002 L 568.9623392491484 138.27121316400002" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g transform="translate(581.3770937571484,138.27121316400002) translate(-581.3770937571484,-138.27121316400002)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 568.8115527571483 131.98844266400002 L 581.3770937571484 138.27121316400002 L 568.8115527571483 144.55398366400001 Z"/></g></g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 7.5393246000000005 160.88918696400003 L 438.90650099992183 160.88918696400003 L 452.47728527992183 174.45997124400003 L 452.47728527992183 255.88467692400002 L 7.5393246000000005 255.88467692400002 L 7.5393246000000005 160.88918696400003 M 438.90650099992183 160.88918696400003 L 438.90650099992183 174.45997124400003 L 452.47728527992183 174.45997124400003" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.64943347999997" y="181.99929584400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">Setup ={</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.64943347999997" y="197.07794504400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">  url:'.../Node/resource/convert/{UUID}/xlsx?version={version}',</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.64943347999997" y="212.15659424400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">  sourceFormat:'ILCD',</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.64943347999997" y="227.23524344400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">  targetFormat:'Excel'</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.64943347999997" y="242.31389264400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">}</text></g><g><g><rect fill="white" stroke="none" x="252.7141203523437" y="278.50265072400003" width="305.95715799242186" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="254.9759177323437" y="292.0734350040001" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">name of a zipFile that should contain Excel file</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 581.3770937571484 298.10489468400004 L 242.4230594479609 298.10489468400004" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="6.03145968"/><g transform="translate(230.0083049399609,298.10489468400004) translate(-230.0083049399609,-298.10489468400004)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 242.5738459399609 291.822124184 L 230.0083049399609 298.10489468400004 L 242.5738459399609 304.38766518400007 Z"/></g></g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 581.3770937571484 338.06331506400005 L 588.9164183571484 338.06331506400005 M 588.9164183571484 320.72286848400006 L 659.9172617492577 320.72286848400006 L 673.4880460292577 334.29365276400006 L 673.4880460292577 355.40376164400004 L 588.9164183571484 355.40376164400004 L 588.9164183571484 320.72286848400006 M 659.9172617492577 320.72286848400006 L 659.9172617492577 334.29365276400006 L 673.4880460292577 334.29365276400006" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="610.0265272371483" y="341.83297736400004" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">zipFile</text></g><g><g><rect fill="white" stroke="none" x="334.19658037431634" y="378.02173544400006" width="142.99223794847657" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="336.45837775431636" y="391.5925197240001" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">[GET] /result/{zipFile}</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 230.0083049399609 397.62397940400007 L 568.9623392491484 397.62397940400007" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g transform="translate(581.3770937571484,397.62397940400007) translate(-581.3770937571484,-397.62397940400007)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 568.8115527571483 391.34120890400004 L 581.3770937571484 397.62397940400007 L 568.8115527571483 403.9067499040001 Z"/></g></g><g><g><rect fill="white" stroke="none" x="340.3832640291015" y="420.2419532040001" width="130.61887063890626" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="342.6450614091015" y="433.8127374840001" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">"application/zip" file</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 581.3770937571484 425.3431072105524 L 242.32143666625774 468.9938366600227" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="6.03145968"/><g transform="translate(230.0083049399609,470.5790548105524) rotate(-7.336024878107799,0,0) translate(-230.0083049399609,-470.5790548105524)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 242.5738459399609 464.2962843105524 L 230.0083049399609 470.5790548105524 L 242.5738459399609 476.86182531055243 Z"/><g transform="rotate(7.336024878107799,0,0)"/></g></g></g><g/><g/></g></svg>
 *
 * <svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="794" height="809"><defs/><g><g/><g/><g/><g/><g/><g/><g/><g/><g/><g/><g/><g/><g/><g/><g/><g><rect fill="white" stroke="none" x="0" y="0" width="794" height="809"/></g><g><text fill="black" stroke="none" font-family="sans-serif" font-size="16.5pt" font-style="normal" font-weight="normal" text-decoration="normal" x="329.17292131154494" y="22.6179738" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">XLSX ⇨ XML</text></g><g/><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 212.105507585 88.511670804 L 212.105507585 809.5813610252255" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="11.598960923076923,5.0262164"/><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 553.7488020174219 88.511670804 L 553.7488020174219 809.5813610252255" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="11.598960923076923,5.0262164"/><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 716.4007353378672 88.511670804 L 716.4007353378672 809.5813610252255" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="11.598960923076923,5.0262164"/></g><g><path fill="none" stroke="none"/><g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 161.52340196276563 47.799317964000004 L 262.6876132072344 47.799317964000004 L 262.6876132072344 88.511670804 L 161.52340196276563 88.511670804 L 161.52340196276563 47.799317964000004 Z" stroke-miterlimit="10" stroke-width="2.4125838720000004" stroke-dasharray=""/></g><g><g/><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="177.88373634476562" y="73.43302160400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">soda4LCA</text></g><path fill="none" stroke="none"/><g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 476.2980974571992 47.799317964000004 L 631.1995065776446 47.799317964000004 L 631.1995065776446 88.511670804 L 476.2980974571992 88.511670804 L 476.2980974571992 47.799317964000004 Z" stroke-miterlimit="10" stroke-width="2.4125838720000004" stroke-dasharray=""/></g><g><g/><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="492.65843183919924" y="73.43302160400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">conversion-service</text></g><path fill="none" stroke="none"/><g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 646.2781557776445 47.799317964000004 L 786.5233148980899 47.799317964000004 L 786.5233148980899 88.511670804 L 646.2781557776445 88.511670804 L 646.2781557776445 47.799317964000004 Z" stroke-miterlimit="10" stroke-width="2.4125838720000004" stroke-dasharray=""/></g><g><g/><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="662.6384901596446" y="73.43302160400002" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">DS-ZipImporter</text></g></g><g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 212.105507585 143.548740384 L 204.566182985 143.548740384 M 21.110108880000002 118.668969204 L 190.995398705 118.668969204 L 204.566182985 143.548740384 L 190.995398705 168.42851156400002 L 21.110108880000002 168.42851156400002 L 7.5393246000000005 143.548740384 L 21.110108880000002 118.668969204" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.64943348" y="139.779078084" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">Host XLSX on</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.64943348" y="154.857727284" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve"> /xlsxinbound/{filename}</text></g><g><g><rect fill="white" stroke="none" x="232.00605566339843" y="191.046485364" width="301.842198275625" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="234.26785304339842" y="204.617269644" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">[POST] /convert/ URL for locally hosted XLSX</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 212.105507585 210.64872932400002 L 541.3340475094219 210.64872932400002" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g transform="translate(553.7488020174219,210.64872932400002) translate(-553.7488020174219,-210.64872932400002)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 541.1832610174218 204.36595882400002 L 553.7488020174219 210.64872932400002 L 541.1832610174218 216.931499824 Z"/></g></g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 7.745749657148465 233.26670312400006 L 402.89448123285155 233.26670312400006 L 416.46526551285154 246.83748740400006 L 416.46526551285154 328.26219308400005 L 7.745749657148465 328.26219308400005 L 7.745749657148465 233.26670312400006 M 402.89448123285155 233.26670312400006 L 402.89448123285155 246.83748740400006 L 416.46526551285154 246.83748740400006" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.855858537148436" y="254.37681200400004" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">Setup ={</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.855858537148436" y="269.455461204" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">  url:'.../Node/resource/convert/xlsxoutbound/{filename},',</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.855858537148436" y="284.534110404" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">  sourceFormat:'Excel',</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.855858537148436" y="299.61275960399996" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">  targetFormat:'ILCD'</text><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="28.855858537148436" y="314.69140880399993" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">}</text></g><g><g><rect fill="white" stroke="none" x="253.57534582453124" y="391.59251972400006" width="258.70361795335936" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="255.83714320453123" y="405.1633040040001" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">[GET] /convert/xlsxoutbound/{filename}</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 553.7488020174219 411.19476368400007 L 224.52026209299999 411.19476368400007" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g transform="translate(212.105507585,411.19476368400007) translate(-212.105507585,-411.19476368400007)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 224.671048585 404.91199318400004 L 212.105507585 411.19476368400007 L 224.671048585 417.4775341840001 Z"/></g></g><g><g><rect fill="white" stroke="none" x="283.81817419367184" y="433.8127374840001" width="198.21796121507813" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="286.07997157367186" y="447.3835217640001" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">"application/vnd.ms-excel" file</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 212.105507585 446.8851591971077 L 541.3821334665237 475.9508356634096" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="6.03145968"/><g transform="translate(553.7488020174219,477.0424575971077) rotate(5.044498221113359,0,0) translate(-553.7488020174219,-477.0424575971077)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 541.1832610174218 470.7596870971077 L 553.7488020174219 477.0424575971077 L 541.1832610174218 483.32522809710775 Z"/><g transform="rotate(-5.044498221113359,0,0)"/></g></g><g><g><rect fill="white" stroke="none" x="229.948575805" y="522.2784051971078" width="305.95715799242186" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="232.21037318499998" y="535.8491894771078" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">name of a zipFile that should contain Excel file</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 553.7488020174219 541.8806491571078 L 224.52026209299999 541.8806491571078" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="6.03145968"/><g transform="translate(212.105507585,541.8806491571078) translate(-212.105507585,-541.8806491571078)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 224.671048585 535.5978786571078 L 212.105507585 541.8806491571078 L 224.671048585 548.1634196571077 Z"/></g></g><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 553.7488020174219 581.8390695371078 L 561.2881266174219 581.8390695371078 M 561.2881266174219 564.4986229571078 L 632.2889700095312 564.4986229571078 L 645.8597542895312 578.0694072371078 L 645.8597542895312 599.1795161171078 L 561.2881266174219 599.1795161171078 L 561.2881266174219 564.4986229571078 M 632.2889700095312 564.4986229571078 L 632.2889700095312 578.0694072371078 L 645.8597542895312 578.0694072371078" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="582.3982354974219" y="585.6087318371078" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">zipFile</text></g><g><g><rect fill="white" stroke="none" x="311.4310358269726" y="621.7974899171078" width="142.99223794847657" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="313.69283320697264" y="635.3682741971078" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">[GET] /result/{zipFile}</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 212.105507585 641.3997338771078 L 541.3340475094219 641.3997338771078" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g transform="translate(553.7488020174219,641.3997338771078) translate(-553.7488020174219,-641.3997338771078)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 541.1832610174218 635.1169633771078 L 553.7488020174219 641.3997338771078 L 541.1832610174218 647.6825043771078 Z"/></g></g><g><g><rect fill="white" stroke="none" x="317.6177194817578" y="664.0177076771079" width="130.61887063890626" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="319.8795168617578" y="677.5884919571079" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">"application/zip" file</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 553.7488020174219 669.3499234652255 L 224.41284715200624 712.956293734655" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray="6.03145968"/><g transform="translate(212.105507585,714.5858710652255) rotate(-7.54248648630011,0,0) translate(-212.105507585,-714.5858710652255)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 224.671048585 708.3031005652256 L 212.105507585 714.5858710652255 L 224.671048585 720.8686415652255 Z"/><g transform="rotate(7.54248648630011,0,0)"/></g></g><g><g><rect fill="white" stroke="none" x="376.0397173309453" y="737.2038448652255" width="176.42680826097657" height="19.602243960000003"/></g><text fill="black" stroke="none" font-family="sans-serif" font-size="11pt" font-style="normal" font-weight="normal" text-decoration="normal" x="378.3015147109453" y="750.7746291452255" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">Upload zip to default stock</text></g><g><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 212.105507585 756.8060888252255 L 703.9859808298672 756.8060888252255" stroke-miterlimit="10" stroke-width="1.2565541" stroke-dasharray=""/><g transform="translate(716.4007353378672,756.8060888252255) translate(-716.4007353378672,-756.8060888252255)"><path fill="black" stroke="none" paint-order="stroke fill markers" d=" M 703.8351943378672 750.5233183252255 L 716.4007353378672 756.8060888252255 L 703.8351943378672 763.0888593252255 Z"/></g></g><g><g/><path fill="none" stroke="black" paint-order="fill stroke markers" d=" M 159.33023538499998 350.88016688400006 L 606.5240742174219 350.88016688400006 L 606.5240742174219 499.66043139710774 L 159.33023538499998 499.66043139710774 L 159.33023538499998 350.88016688400006 Z" stroke-miterlimit="10" stroke-width="2.154092742857143" stroke-dasharray=""/><path fill="white" stroke="black" paint-order="fill stroke markers" d=" M 159.33023538499998 350.88016688400006 L 159.33023538499998 368.97454592400004 L 207.70652792562498 368.97454592400004 L 216.75371744562497 359.9273564040001 L 216.75371744562497 350.88016688400006 L 159.33023538499998 350.88016688400006" stroke-miterlimit="10" stroke-width="2.154092742857143" stroke-dasharray=""/><text fill="black" stroke="none" font-family="sans-serif" font-size="8.8pt" font-style="normal" font-weight="bold" text-decoration="normal" x="174.40888458499998" y="362.943086244" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">opt</text><text fill="black" stroke="none" font-family="sans-serif" font-size="8.8pt" font-style="normal" font-weight="bold" text-decoration="normal" x="231.83236664562497" y="362.943086244" text-anchor="start" dominant-baseline="alphabetic" xml:space="preserve">[Grab XLSX from Node]</text></g></g><g/><g/><g/></g></svg>
 * <p>
 * <p>
 * FORMATTERON
 *
 * @author MK
 * @see ConvertResource
 * @since soda4LCA 6.3.1
 */

@Service
public class ConversionService {
    // http://localhost/api/convert/
    private static final String API_ENDPOINT_CONVERT = ConfigurationService.INSTANCE.getConvertXLSXAPI() + "/api/convert/";
    // http://localhost/api/result/
    private static final String API_ENDPOINT_RESULT = ConfigurationService.INSTANCE.getConvertXLSXAPI() + "/api/result/";
    private static final Logger LOGGER = LogManager.getLogger(ConversionService.class);
    private static final String ZIP_MIME = "application/zip";

    /**
     * <p>
     * Send a HTTP GET request to given URL.
     * </p>
     *
     * <p>
     * Used as a sanity check before attempting to start conversion flow.
     * </p>
     *
     * @param url       URL to send a request to.
     * @param timeoutms max wait in milliseconds before returning 408 Timeout
     * @return HTTP status code. expected to be 200 OK
     */
    public static int ping(String url, int timeoutms) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeoutms);
            connection.connect();

            return connection.getResponseCode(); // should be 200 OK
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.warn("URL %s is not responsive", url);
        }
        return 500;
    }

    /**
     * <ol>
     * 		<li> Removes extra forward slashes from URL </li>
     * 		<li> check if URL is reachable from the outside </li>
     * </ol>
     * <p>
     * ex: http://localhost:8080/Node//resource/....
     * becomes http://localhost:8080/Node/resource/....
     *
     * @param url with a bunch of extra double forward slashes
     * @return normalized url
     * @throws IOException
     * @see {@link #ping(String, int)}
     */
    private static String normalizeURL(String url) throws IOException {
        try {
            String url_normalized = new URI(url).normalize().toString();
            // check if file is exposed for the Conversion service
            // ping yourself...
            int code = ping(url_normalized, 3000);
            if (code > 200) throw new IOException("url:" + url_normalized + " HTTP code:" + code);

            return url_normalized;

        } catch (URISyntaxException e1) {
            LOGGER.error("invalid URL:" + url);
            throw new IOException("invalid URL:" + url);
        }
    }

    /**
     * @param URL          of the process or XLSX file. Expected to contain a specific
     *                     UUID & version, for example: <code>
     *                     http://localhost:8080/Node/resource/processes/a91683fe-3b0b-471c-a007-f9d8b4e29d5a?format=xml&version=01.00.000,
     *                     </code>
     * @param sourceFormat "ILCD" or "Excel"
     * @param targetFormat "Excel" or "ILCD"
     * @return String name of the file (zip) that contains result of conversion
     * procedure
     * @throws IOException
     */
    public synchronized static String askConversionService(String URL, String sourceFormat, String targetFormat)
            throws RestClientException, IOException {

        URL = normalizeURL(URL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        JsonObject payload = new JsonObject();
        payload.addProperty("url", URL);
        payload.addProperty("sourceFormat", sourceFormat);
        payload.addProperty("targetFormat", targetFormat);
        payload.addProperty("refSystem", "default");

        HttpEntity<String> request = new HttpEntity<String>(payload.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("calling conversion service at " + API_ENDPOINT_CONVERT);
            LOGGER.debug("for URL " + URL);
            LOGGER.debug("source format " + sourceFormat + ", target format " + targetFormat);
        }

        try {
            ResponseEntity<String> jsonResponse = restTemplate.postForEntity(API_ENDPOINT_CONVERT, request, String.class);
            JsonObject obj = new Gson().fromJson(jsonResponse.getBody(), JsonObject.class);
            return obj.get("zipFile").getAsString();
        } catch (JsonSyntaxException | ClassCastException | IllegalStateException e) {
            LOGGER.error("Failed to parse the JSON response");
        }

        return null;
    }

    /**
     * @param filename of the requested file on the conversion service. expected to
     *                 be a random UUID (different from process UUID) appended by
     *                 .zip.
     * @return byte[] with the content of the requests file from conversion
     * service
     */

    public synchronized static byte[] fetchConversionResult(@PathParam("filename") String filename)
            throws RestClientException {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> byteResponse = restTemplate.getForEntity(API_ENDPOINT_RESULT + filename, byte[].class);
        return byteResponse.getBody();
    }


    /**
     * Ask ConversionService to convert an excel sheet into a zipfile containing
     * (a bunch of) XMLs.
     *
     * @param xlsxInfo the info bean with the uploaded file.
     *
     * <p>
     * <b>SIDE EFFECT--</b> Writes down the zipfile in
     * <code>XLSX_TMPDIR</code> so that it can be treated seamlessly as
     * an ordinary zip import by <code>importHandler</code>
     * </p>
     * @throws Exception
     */

    public static void XLSX2XML(UploadedFileInformation xlsxInfo) throws IOException {
        String xlsxFileName = xlsxInfo.getFileName();
        String xlsxURL = ConvertResource.OUTBOUND_ENDPOINT + xlsxFileName;

        byte[] zipFile = null;
        String zipFileName = null;

        try {

            // "http://localhost:8080/Node/resource/convert/xlsx/a91683fe-3b0b-471c-a007-f9d8b4e29d5a.xlsx"
            // 1st request to conversion service
            zipFileName = ConversionService.askConversionService(xlsxURL, "Excel", "ILCD");

            // 2nd request to conversion service
            zipFile = ConversionService.fetchConversionResult(zipFileName);
        } catch (ResourceAccessException e) {
            LOGGER.error("Connection to conversion service has been refused");
            throw new IOException("Conversion Service: Connection refused (maybe 'feature.convertXLSXAPI.URL' is not set?)");
        } catch (RestClientException e) {
            LOGGER.error("Conversion Service failed to parse given XLSX");
            throw new IOException("Conversion Service: Failed to parse given XLSX (maybe there are missing flows?)");
        }

        if (zipFileName == null)
            throw new IOException("Conversion Service: empty zipFileName (maybe conversion still in progress?)");

        try {

            Path tmpPath = Paths.get(ConvertResource.XLSX_TMPDIR, xlsxInfo.getSubFolder(), xlsxFileName + ".zip");
            Files.createParentDirs(tmpPath.toFile());
            Files.write(zipFile, tmpPath.toFile());

            xlsxInfo.setFileName(tmpPath.getFileName().toString());
            xlsxInfo.setPathName(tmpPath.toString());
            xlsxInfo.setFileSize(zipFile.length);
            xlsxInfo.setFileType(ZIP_MIME);
        } catch (IOException e) {
            LOGGER.error("Failed to write conversion result to provided temporary directory, upload process not complete.");
            throw new IOException("Conversion Service: Failed to write zip to provided temporary directory (maybe writing permission issue?)");
        }

    }

}