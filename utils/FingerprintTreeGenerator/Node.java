
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections.map.MultiValueMap;

/**
 *
 * @author sjobe
 */
class Node {

  int query;
  HashMap<String, DNSServer> uniqueHits;
  MultiValueMap multipleHits;
  HashMap<String, Node> children;

  Node() {
    uniqueHits = new HashMap<String, DNSServer>();
    multipleHits = new MultiValueMap();
    children = new HashMap<String, Node>();
  }

  @SuppressWarnings("unchecked") //Else we get a warning about unchecked cast
  String getXML(ArrayList<String> responses) {
    StringBuilder sb = new StringBuilder();
    sb.append("<query num=\"");
    sb.append(this.query);
    sb.append("\">\n");

    for (String r : this.uniqueHits.keySet()) {
      sb.append("<response id=\"");
      sb.append(this.addResponseToArrayList(responses, r));
      sb.append("\">");
      sb.append(this.uniqueHits.get(r).name);
      sb.append("</response>\n");
    }

    for (Object r : this.multipleHits.keySet()) {

      sb.append("<response id=\"");
      sb.append(this.addResponseToArrayList(responses, (String) r));
      sb.append("\">");
      sb.append(getServersString((List<DNSServer>) this.multipleHits.get(r)));
      sb.append("</response>\n");
    }

    for (String r : children.keySet()) {
      sb.append("<response id=\"");
      sb.append(this.addResponseToArrayList(responses, r));
      sb.append("\">\n");
      sb.append(children.get(r).getXML(responses));
      sb.append("  </response>\n");

    }

    sb.append("</query>\n");

    return sb.toString();
  }

  String getPerlFPDNSFormat(ArrayList<String> responses, HashMap<Integer, Integer> queriesArrayMap) {
    StringBuilder sb = new StringBuilder();
    String s;
    for (String r : children.keySet()) {
      int index = this.addQueryIndexToArrayMap(this.query, queriesArrayMap);
      s = "{ fingerprint=>$iq[" + this.addResponseToArrayList(responses, r) + "], header=>$qy["+index+"], ";
      s += "query=>$ntc["+index+"], ";
      s += "ruleset => [\n";
      s += children.get(r).getPerlFPDNSFormat(responses, queriesArrayMap);
      s += "]},\n";
      sb.append(s);
    }

    for (String r : this.uniqueHits.keySet()) {
      s = "{ fingerprint => $iq[" +  this.addResponseToArrayList(responses, r) + "], result => { vendor =>\"VENDOR\", product=>\"" + this.uniqueHits.get(r).name + "\",version=>\"VERSION\"}, },\n";
      sb.append(s);
    }

    for (Object r : this.multipleHits.keySet()) {
      s = "{ fingerprint => $iq[" +  this.addResponseToArrayList(responses, (String) r) + "], result => { vendor =>\"VENDOR\", product=>\"" + getServersString((List<DNSServer>) this.multipleHits.get(r)) + "\",version=>\"VERSION\"}, },\n";
      sb.append(s);
    }

    return sb.toString();
  }

  int addResponseToArrayList(ArrayList<String> responses, String response) {
    if (!responses.contains(response)) {
      responses.add(response);
    }
    return responses.indexOf(response);
  }

  int addQueryIndexToArrayMap(int queryIndex, HashMap<Integer, Integer> queriesArrayMap){
    if(!queriesArrayMap.containsKey(queryIndex)){
      queriesArrayMap.put(queryIndex, queriesArrayMap.size());
    }
    return queriesArrayMap.get(queryIndex);
  }

  String getServersString(List<DNSServer> servers) {
    String s = "";
    for (DNSServer sv : servers) {
      s += sv.name + ", ";//TODO: Use a stringbuilder here ?
    }
    return s;
  }
}
