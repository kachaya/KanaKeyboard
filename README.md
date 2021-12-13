# KanaKeyboard - かな配列キーボード

## 概要

PCのJISかな配列のようにかな文字キーを配置したAndroid用のかな漢字変換IMEです。。


## ライセンス等

### KanaKeyboard
本ソフトウェアには Apache ライセンスが適用されます。

```
Copyright 2021 kachaya

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```



### MapDB
本ソフトウェアはデータベースエンジンとして [MapDB](https://github.com/jankotek/MapDB/tree/release-1.0) Version 1.0.9 を使用しています。

```
MapDB
Copyright 2012-2014 Jan Kotek

This product includes software developed by Thomas Mueller and H2 group
Relicensed under Apache License 2 with Thomas permission.
(CompressLZF.java and EncryptionXTEA.java)
Copyright (c) 2004-2011 H2 Group

This product includes software developed by Doug Lea and JSR 166 group:
(LongConcurrentMap.java, Atomic.java)

 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain


This product includes software developed for Apache Solr
(LongConcurrentLRUMap.java)
Copyright 2006-2014 The Apache Software Foundation

This product includes software developed for Apache Harmony
(LongHashMap.java)
Copyright 2008-2012 The Apache Software Foundation


This product includes software developed by Nathen Sweet for Kryo
Relicensed under Apache License 2 (or later) with Nathans permission.
(DataInput2.packInt/Long and DataOutput.unpackInt/Long methods)
Copyright (c) 2012 Nathan Sweet

This product includes software developed for Android project
(SerializerPojo, a few lines to invoke constructor, see comments)
//Copyright (C) 2012 The Android Open Source Project, licenced under Apache 2 license

This product includes software developed by Heinz Kabutz for javaspecialists.eu
(SerializerPojo, a few lines to invoke constructor, see comments)
2010-2014 Heinz Kabutz

Some Map unit tests are from  Google Collections.
Credit goes to Jared Levy, George van den Driessche and other Google Collections developers.
Copyright (C) 2007 Google Inc.

Luc Peuvrier wrote some unit tests for ConcurrerentNavigableMap interface.
```



### SKK辞書
本ソフトウェアではSKK-JISYO.LをMapDB形式のファイルに変換したものを同梱しています。

```
;; Large size dictionary for SKK system
;; Copyright (C) 1988-1995, 1997, 1999-2014
;;
;; Masahiko Sato <masahiko@kuis.kyoto-u.ac.jp>
;; Hironobu Takahashi <takahasi@tiny.or.jp>,
;; Masahiro Doteguchi, Miki Inooka,
;; Yukiyoshi Kameyama <kameyama@kuis.kyoto-u.ac.jp>,
;; Akihiko Sasaki, Dai Ando, Junichi Okukawa,
;; Katsushi Sato and Nobuhiro Yamagishi
;; NAKAJIMA Mikio <minakaji@osaka.email.ne.jp>
;; MITA Yuusuke <clefs@mail.goo.ne.jp>
;; SKK Development Team <skk@ring.gr.jp>
;;
;; Maintainer: SKK Development Team <skk@ring.gr.jp>
;; Keywords: japanese
;;
;; This dictionary is free software; you can redistribute it and/or
;; modify it under the terms of the GNU General Public License as
;; published by the Free Software Foundation; either version 2, or
;; (at your option) any later version.
;;
;; This dictionary is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;; General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Daredevil SKK, see the file COPYING.  If not, write to
;; the Free Software Foundation Inc., 59 Temple Place - Suite 330,
;; Boston, MA 02111-1307, USA.
```



辞書ファイルを変換するのに使用したコードを以下に示します。

```java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class DicMaker {
	static final String SKK_JISYO_SOURCE = "SKK-JISYO.L";
	static final String SKK_JISYO_CHARSET = "EUC-JP";
	static final String SKK_JISYO_TEXT = "entry.txt";
	static final String MAP_NAME = "dic";
	static final String DB_NAME = "main_dic.db";
	static public void main(String[] argv) {
		try {
			DB db = DBMaker.newFileDB(new File(DB_NAME)).closeOnJvmShutdown().transactionDisable().make();
			HTreeMap<String, String> map = db.getHashMap(MAP_NAME);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(SKK_JISYO_SOURCE), SKK_JISYO_CHARSET));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(SKK_JISYO_TEXT)));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (line.startsWith(";;")) {
					continue;
				}
				int idx = line.indexOf(' ');
				if (idx == -1) {
					continue;
				}
				String key = line.substring(0, idx);
				String value = line.substring(idx + 1);
				if (key.contains("う゛")) {
					key = key.replace("う゛", "\u3094"); // "ゔ"
				}
				String data[] = value.split("/");
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < data.length; i++) {
					if (data[i].length() == 0) {
						continue;
					}
					int aidx = data[i].indexOf(';');
					if (aidx != -1) {
						data[i] = data[i].substring(0, aidx);
					}
					if (data[i].contains("(concat ")) {
						String parts[] = data[i].split("\"");
						data[i] = parts[1].replace("\\057", "/");
					}
					if (sb.length() != 0) {
						sb.append("\t");
					}
					sb.append(data[i]);
				}
				value = sb.toString();
				map.put(key, value);
				bw.write(key + "\t" + value + "\n");
			}
			bw.close();
			db.commit();
			db.close();
			br.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

```

