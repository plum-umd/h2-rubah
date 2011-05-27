/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 */

var pages=new Array();
var ref=new Array();
var firstLink = null;
var firstLinkWord = null;

String.prototype.endsWith = function(suffix) {
    var startPos = this.length - suffix.length;
    if (startPos < 0) {
      return false;
    }
    return (this.lastIndexOf(suffix, startPos) == startPos);
};

function listWords(value, open) {
  value = replaceOtherChars(value);
  value = trim(value);
  if(pages.length==0) {
    load();
  }
  var table = document.getElementById('result');
  while(table.rows.length > 0) {
    table.deleteRow(0);
  }
  firstLink=null;
  var clear = document.getElementById('clear');
  if(value.length == 0) {
    clear.style.display = 'none';
    return true;
  }
  clear.style.display = '';
  var keywords = value.split(' ');
  if(keywords.length > 1) {
    listAnd(keywords);
    return true;
  }
  if(value.length < 3) {
    max = 100;
  } else {
    max = 1000;
  }
  value = value.toLowerCase();
  var r = ref[value.substring(0,1)];
  if(r==undefined) {
    return true;
  }
  var x=0;
  var words = r.split(';');
  var count=0;
  for(var i=0; i<words.length; i++) {
    var wordRef = words[i];
    if(wordRef.toLowerCase().indexOf(value)==0) {
      count++;
    }
  }
  for(var i=0; i<words.length && (x<=max); i++) {
    var wordRef = words[i];
    if(wordRef.toLowerCase().indexOf(value)==0) {
      word = wordRef.split("=")[0];
      var tr = table.insertRow(x++);
      var td = document.createElement('td');
      var tdClass = document.createAttribute('class');
      tdClass.nodeValue = 'searchKeyword';
      td.setAttributeNode(tdClass);

      var ah = document.createElement('a');
      var href = document.createAttribute('href');
      href.nodeValue = 'javascript:set("' + word + '");';
      var link = document.createTextNode(word);
      ah.setAttributeNode(href);
      ah.appendChild(link);
      td.appendChild(ah);
      tr.appendChild(td);
      piList = wordRef.split("=")[1].split(",");
      if(count<20 || open==word) {
        x = addReferences(x, piList, word);
      }
    }
  }
  if(x==0) {
      noResults(table);
  }
  return true;
}

function set(v) {
    if(pages.length==0) {
        load();
    }
    var search = document.getElementById('search').value;
    listWords(search, v);
    document.getElementById('search').focus();
    window.scrollBy(-20, 0);
}

function goFirst() {
    var table = document.getElementById('result');
    if(firstLink != null) {
        go(firstLink, firstLinkWord);
    }
    return false;
}

function go(pageId, word) {
    var page = pages[pageId];
      var load = '../' + page.file + '?highlight=' + encodeURIComponent(word);
      if(!top.main.location.href.endsWith(page.file)) {
        top.main.location = load;
      }
}

function listAnd(keywords) {
  var count = new Array();
  var weight = new Array();
  for(var i=0; i<pages.length; i++) {
    count[i] = 0;
    weight[i] = 0;
  }
  for(var i=0; i<keywords.length; i++) {
    var value = keywords[i].toLowerCase();
    var r = ref[value.substring(0,1)];
    if(r==undefined) {
      return true;
    }
    var words = r.split(';');
    for(var j=0; j<words.length; j++) {
      var wordRef = words[j];
      if(wordRef.toLowerCase().indexOf(value)==0) {
        piList = wordRef.split("=")[1].split(",");
        var w=1;
        for(var k=0; k<piList.length; k++) {
          var pi = piList[k];
          if(pi.charAt(0) == 't') {
            pi = pi.substring(1);
            w=10000;
          } else if(pi.charAt(0) == 'h') {
            pi = pi.substring(1);
            w=100;
          } else if(pi.charAt(0) == 'r') {
            pi = pi.substring(1);
            w=1;
          }
          if(count[pi]>=i) {
            if(count[pi]==i) {
              count[pi]++;
            }
            weight[pi]+=w;
          }
        }
      }
    }
  }
  var x = 0;
  var table = document.getElementById('result');
  var piList = new Array();
  var piWeight = new Array();
  for(var i=0; i<pages.length; i++) {
    if(count[i] >= keywords.length) {
      piList[x] = '' + i;
      piWeight[x] = weight[i];
      x++;
    }
  }
  // sort
  for (var i = 1, j; i < x; i++) {
    var tw = piWeight[i];
    var ti = piList[i];
    for (j = i - 1; j >= 0 && (piWeight[j] < tw); j--) {
      piWeight[j + 1] = piWeight[j];
      piList[j + 1] = piList[j];
    }
    piWeight[j + 1] = tw;
    piList[j + 1] = ti;
  }
  addReferences(0, piList, keywords);
  if(piList.length == 0) {
    noResults(table);
  }
}

function addReferences(x, piList, word) {
  var table = document.getElementById('result');
  for(var j=0; j<piList.length; j++) {
    var pi = piList[j];
    if(pi.charAt(0) == 't') {
      pi = pi.substring(1);
    } else if(pi.charAt(0) == 'h') {
      pi = pi.substring(1);
    } else if(pi.charAt(0) == 'r') {
      pi = pi.substring(1);
    }
    var tr = table.insertRow(x++);
    var td = document.createElement('td');
    var tdClass = document.createAttribute('class');
    tdClass.nodeValue = 'searchLink';
    td.setAttributeNode(tdClass);
    var ah = document.createElement('a');
    var href = document.createAttribute('href');
    var thisLink = 'javascript:go(' + pi + ', "' + word + '")';
    if(firstLink==null) {
      firstLink = pi;
      firstLinkWord = word;
    }
    href.nodeValue = thisLink;
    ah.setAttributeNode(href);
    var page = pages[pi];
    var link = document.createTextNode(page.title);
    ah.appendChild(link);
    td.appendChild(ah);
    tr.appendChild(td);
  }
  return x;
}

function trim(s) {
    while(s.charAt(0)==' ' && s.length>0) {
        s=s.substring(1);
    }
    while(s.charAt(s.length-1)==' ' && s.length>0) {
        s=s.substring(0, s.length-1);
    }
    return s;
}

function replaceOtherChars(s) {
    var x = "";
    for(var i=0; i<s.length; i++) {
        var c = s.charAt(i);
        if("\t\r\n\"'.,:;!&/\\?%@`[]{}()+-=<>|*^~#$".indexOf(c) >= 0) {
            c = " ";
        }
        x += c;
    }
    return x;
}

function noResults(table) {
  var tr = table.insertRow(0);
  var td = document.createElement('td');
  var tdClass = document.createAttribute('class');
  tdClass.nodeValue = 'searchKeyword';
  td.setAttributeNode(tdClass);
  var text = document.createTextNode('No results found');
  td.appendChild(text);
  tr.appendChild(td);
}

