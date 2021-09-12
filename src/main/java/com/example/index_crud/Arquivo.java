package com.example.index_crud;

import aed3.HashExtensivel;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;


public class Arquivo <T extends registros> {//obs tipos genericos sao interfaces n classes a classe que implementa a interface
    RandomAccessFile arquivo;
    Constructor<T> constructor;
    HashExtensivel<ParIDEndereco> indiceDireto;
    final int TAM_CABECALHO = 4;

    public Arquivo(String nomeArquivo, Constructor<T> c) throws Exception {
        File f = new File("dados");
        if (!f.exists()) {
            f.mkdir();
        }
        f = new File("dados/" + nomeArquivo);
        if (!f.exists()) {
            f.mkdir();
        }
        arquivo = new RandomAccessFile("dados/" + nomeArquivo + "/arquivo.db", "rw");
        constructor = c;
        if (arquivo.length() == 0) {
            arquivo.writeInt(0);
        }
        File d = new File("dados");
        if (!d.exists())
            d.mkdir();
        indiceDireto = new HashExtensivel<>(ParIDEndereco.class.getConstructor(), 4,
                "dados/" + nomeArquivo + "/indicedireto.hash_d.db",
                "dados/" + nomeArquivo + "/indicedireto.hash_c.db");

    }

    public int create(T object) throws  Exception {

        T obj= constructor.newInstance();
        arquivo.seek(0);
        int ultimoID = arquivo.readInt();
        int proximoID = ultimoID + 1;
        arquivo.seek(0);
        arquivo.writeInt(proximoID);



        arquivo.seek(arquivo.length());///move para o fim do arquivo para inserir o novo registro

        object.setId(proximoID);
        byte[] ba = object.toByteArray();

        arquivo.writeByte(' ');///lapide


        indiceDireto.create(new ParIDEndereco(proximoID,arquivo.getFilePointer()));///coloca na hash extensivel o id e a posicao



        //tebho que escrever no indice o id e o endereco
        arquivo.writeInt(ba.length);//tamanho do vetor
        arquivo.write(ba);//screve o vetor de bytes

        return proximoID;
    }

    public T read (int idProcurado) throws Exception {

        ///passo o id do indice como parametro e faco um seek direto na posicao
        // que eu quero
        arquivo.seek(TAM_CABECALHO);//Pular o cabecalho e ir para o registro;
        T object= constructor.newInstance();
        byte lapide;
        int tam;
        byte[] ba;

        ParIDEndereco read = indiceDireto.read(idProcurado);/// procura o objeto da hash extensivel com os dados
        if(read!=null) {
            long posicao = read.getLocal();///retorna a posicao do arquivo 'Livros' dada no arquivo indices

            arquivo.seek(posicao-1);/// muda o ponteiro do arquivo

            lapide = arquivo.readByte();
            tam = arquivo.readInt();

            if (lapide == ' ') {  //verifica o lapide

                ba = new byte[tam];
                arquivo.read(ba);
                object.fromByteArray(ba);
                return object;

            }
        }
       /* while(arquivo.getFilePointer() < arquivo.length()){

            lapide = arquivo.readByte();
            tam = arquivo.readInt();

            if(lapide == ' ') {

                ba = new byte[tam];
                arquivo.read(ba);
                object.fromByteArray(ba);

                if(object.getID() == idProcurado)
                    return object;
            } else
                arquivo.skipBytes(tam);

        }*/

       return null;
    }

    /**
     * @param ld - Id de do objeto que sera excluido
     * @return true- se ofi excluido ou false se o objeto nao foi encontrado
     */
    public boolean delete(int ld) throws Exception {

        /// mesma mudanca do read
        ///passo o id do indice como parametro e faco um seek direto na
        // posicao que eu quero


        // Movendo o ponteiro para primeiro registro (após cabeçalho).
        arquivo.seek(TAM_CABECALHO);

        ParIDEndereco read = indiceDireto.read(ld);/// procura o objeto da hash extensivel com os dados
        long posicao = read.getLocal();///retorna a posicao do arquivo 'Livros' dada no arquivo indices
        arquivo.seek(posicao-1);/// muda o ponteiro do arquivo


        // Leitura de Lapide & Tamanho do Registro.
        byte lapide = arquivo.readByte();
        int tam = arquivo.readInt();

        if(lapide == ' ') {
            // extraindo obj.
            T obj = constructor.newInstance();
            byte[] ba = new byte[tam];
            arquivo.read(ba);
            obj.fromByteArray(ba); //le o objeto
            if(obj.getID() == ld) {
                // Retornando a posicao da lapide e marcando como excluido.
                arquivo.seek(posicao-1);
                ///eu alem de marcar como excluido eu preciso retirar do incide
                //(so passo o id pro indice) id: 5 pos: "" mais ou menos
                indiceDireto.delete(obj.getID());
                arquivo.writeByte('*');/// marca como excluido
                return true;
            }
        }

        /*while(arquivo.getFilePointer() < arquivo.length()) {
            // Salvando posição do ponteiro.
            long pos = arquivo.getFilePointer();

            // Leitura de Lapide & Tamanho do Registro.
            byte lapide = arquivo.readByte();
            int tam = arquivo.readInt();

            if(lapide == ' ') {
                // extraindo obj.
                T obj = constructor.newInstance();
                byte[] ba = new byte[tam];
                arquivo.read(ba);
                obj.fromByteArray(ba); //le o objeto
                if(obj.getID() == ld) {
                    // Retornando a posicao da lapide e marcando como excluido.
                    arquivo.seek(pos);
                    ///eu alem de marcar como excluido eu preciso retirar do incide
                    //(so passo o id pro indice) id: 5 pos: "" mais ou menos

                    indiceDireto.delete(obj.getID());
                    arquivo.writeByte('*');/// marca como excluido
                    return true;
                }
            }
        }*/
        return false;
    }

    public boolean update(T novoObj) throws Exception {
        arquivo.seek(TAM_CABECALHO);//Pular o cabecalho e ir para o registro;

        T obj = constructor.newInstance();
        int tam;
        byte lapide;
        byte[] ba;
        byte[] novoBA;
        long position;
        int idProcurado = novoObj.getID();

        ParIDEndereco read = indiceDireto.read(idProcurado);/// procura o objeto da hash extensivel com os dados
        long posicao = read.getLocal();///retorna a posicao do arquivo 'Livros' dada no arquivo indices
        arquivo.seek(posicao-1);/// muda o ponteiro do arquivo

        //pega a posicao do ponteiro
        position = arquivo.getFilePointer();///pega o ponteiro
        lapide= arquivo.readByte();///LE O LAPIDE
        tam = arquivo.readInt();///LE O TAMANHO DO VETOR DE BYTES

        if(lapide == ' '){

            //Tira o objeto desatualizado do registro
            ba = new byte[tam];
            arquivo.read(ba);
            obj.fromByteArray(ba);

            //verifica se e o objeto necessario
            if(obj.getID() == idProcurado){

                //cria um novo registro para o Novo objeto
                novoBA = novoObj.toByteArray();
                int novoTam = novoBA.length;

                //se o tamanho for menor ou igual ele ocupa o mesmo espaço
                if(novoBA.length <= tam){// registro do mesmo tamanho ou menor
                    //se ficar no mesmo lugar eu nao mudo o indice
                    arquivo.seek(posicao + 5);
                    arquivo.write(novoBA);
                    //se nao ele exclui esse registro e coloca no final do arquivo
                }else{//registro aumentou

                    ///se mudar de lugar eu escrevo no indice a nova posicao
                    ///update do indice -> mesmo id mas nova pos

                    long newPointer;

                    arquivo.seek(posicao);
                    arquivo.writeByte('*');//marca como deletado aquela pos
                    arquivo.seek(arquivo.length());
                    newPointer = arquivo.getFilePointer();

                    read = indiceDireto.read(idProcurado);
                    read.setLocal(newPointer+1);
                    indiceDireto.update(read);



                    arquivo.writeByte(' ');
                    arquivo.writeInt(novoTam);
                    arquivo.write(novoBA);
                }

                return true;
            }
        }


       /* while (arquivo.getFilePointer() < arquivo.length()){/// enquanto n EOF
            //pega a posicao do ponteiro
            position = arquivo.getFilePointer();///pega o ponteiro
            lapide= arquivo.readByte();///LE O LAPIDE
            tam = arquivo.readInt();///LE O TAMANHO DO VETOR DE BYTES

            if(lapide == ' '){

                //Tira o objeto desatualizado do registro
                ba = new byte[tam];
                arquivo.read(ba);
                obj.fromByteArray(ba);

                //verifica se e o objeto necessario
                if(obj.getID() == idProcurado){

                    //cria um novo registro para o Novo objeto
                    novoBA = novoObj.toByteArray();
                    int novoTam = novoBA.length;

                    //se o tamanho for menor ou igual ele ocupa o mesmo espaço
                    if(novoBA.length <= tam){// registro do mesmo tamanho ou menor
                        //se ficar no mesmo lugar eu nao mudo o indice
                        arquivo.seek(position + 5);
                        arquivo.write(novoBA);
                        //se nao ele exclui esse registro e coloca no final do arquivo
                    }else{//registro aumentou

                        ///se mudar de lugar eu escrevo no indice a nova posicao
                        ///update do indice -> mesmo id mas nova pos

                        arquivo.seek(position);
                        arquivo.writeByte('*');//marca como deletado aquela pos
                        arquivo.seek(arquivo.length());
                        arquivo.writeByte(' ');
                        arquivo.writeInt(novoTam);
                        arquivo.write(novoBA);
                    }

                    return true;
                }
            }
        }*/
        return false;
    }


    private T leEntidade(long pos) throws Exception{
        T obj = constructor.newInstance();
        byte ba[];
        arquivo.seek(pos);
        byte lapide= arquivo.readByte();///LE O LAPIDE
        int tam = arquivo.readInt();///LE O TAMANHO DO VETOR DE BYTES

        if(lapide == ' ') {

            //Tira o objeto desatualizado do registro
            ba = new byte[tam];
            arquivo.read(ba);
            obj.fromByteArray(ba);
            return obj;
        }else{
            arquivo.skipBytes(tam);
        }

        return null;
    }


}
