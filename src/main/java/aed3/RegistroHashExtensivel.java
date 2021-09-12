/*
REGISTRO HASH EXTENSÍVEL

Esta interface apresenta os métodos que os objetos
a serem incluídos na tabela hash extensível devem
conter.

Implementado pelo Prof. Marcos Kutova
v1.1 - 2021
*/
package aed3;

import java.io.IOException;

public interface RegistroHashExtensivel<T> {

    public int hashCode(); // chave numérica para ser usada no diretório

    public long getLocal();// retorna o enderco no arquivo de bytes
    public void setLocal(long endereco);// seta o endereco como deletado -1

    public short size(); // tamanho FIXO do registro

    public byte[] toByteArray() throws IOException; // representação do elemento em um vetor de bytes

    public void fromByteArray(byte[] ba) throws IOException; // vetor de bytes a ser usado na construção do elemento

}
