UPDATE networknode SET baseurl = REPLACE(baseurl, '/resource/', '/') WHERE baseurl LIKE ('%/resource/');
