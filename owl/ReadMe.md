ReadMe.txt

1.起源
使用“百度百科-英文维基百科”数据集，构建一个跨语言、大规模的、领域无关的中英文文本体。
知识工程，project1

2.功能
根据百科和维基百科的语料，构建本体模型，并进行一定的统计和查询。

3.目录和文件说明
owl                           	--根目录  
├── compile.sh                 	--编译脚本  
├── run.sh                 		--运行脚本
├── ReadMe.txt					--使用说明
├── bin/						--java编译生成的class文件目录
├── data/						--百度百科、英文维基百科结构化数据目录（需要把6个文件放在此处）
├── lib/						--jena API的jar包目录（需要jena的jar文件放在此处）
├── etc/						--生成的本体模型及其他文件的目录
└── src/						--源代码
    ├── ArticleReader.java      --处理百度Article、维基Article的类
    ├── CategoryReader.java     --处理百度Category、维基Category的类
    ├── CreateOntology.java     --程序入口，包含main函数，创建本体，进行统计和查询等
    ├── LinkReader.java         --处理百度、维基中英文对照的类
    ├── SPARQL.java              --进行SPARQL查询的类
    ├── Statistics.java         --进行统计的类
    └── XmlFilter.java          --进行特殊字符的过滤
        
4.使用说明
运行环境；
	配置好java的linux环境（如ubuntu）
	将6个百科和维基百科的语料文件放在owl/data下
	将jena的全部jar文件放在owl/lib下
运行：
	./compile.sh
	./run.sh

5.作者
Copyright © izhiqiu@foxmail.com
