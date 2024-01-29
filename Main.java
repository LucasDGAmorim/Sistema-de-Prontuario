import java.io.RandomAccessFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.concurrent.ThreadLocalRandom;
import java.util.*;

//Programa principal
public class Main {

	//Método de inserção de chaves para testes
	static void inserirPaciente(RandomAccessFile arq,Diretorio1 dir,int tamAnotacoes,long bytesPorPaciente,int cpf) throws IOException{
		Paciente pacienteNovo;
		byte [] ba;
		int v = 0;
		int aux;
		boolean verificaLapide;
		boolean existeLapide;
		boolean irParaUltimaPos;
		int qtdRegistros;
		int primeiraPosRegistros = 9;
		arq.seek(0);
		qtdRegistros = arq.readInt();//Leio a quantidade de registros
		if(qtdRegistros == 0){//Não existe PACIENTE
			++qtdRegistros;
			pacienteNovo = new Paciente(qtdRegistros,tamAnotacoes,cpf);
			ba = pacienteNovo.toByteArray();//Vetor de bytes recebe a entidade paciente
			arq.seek(primeiraPosRegistros);//Primeira posição de escrita
			arq.write(ba);//Escrevo meu registro no arquivo
			arq.seek(0);//Posição com a quantidade de registros
			arq.writeInt(qtdRegistros);//Atualizo quantidade de registros no arquivo
			dir.inserir_chave(pacienteNovo.cpf, qtdRegistros);
		}
		else{//existe PACIENTE
			arq.seek(4);
			existeLapide = arq.readBoolean();
			irParaUltimaPos = true;
			if(existeLapide == true){//Procurar primeiro registro lixo
				for(int i = 0; i < qtdRegistros && v != 1 ; i++){
					arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente ) );//Verifico cada registro
					verificaLapide = arq.readBoolean();
					if(verificaLapide == true){
						int x = qtdRegistros;//Nao atualizo a quantidade de registros pois eh uma lapide
						arq.seek(primeiraPosRegistros + 1 + (i * bytesPorPaciente ) );
						int novoId = arq.readInt();
						pacienteNovo = new Paciente(novoId,tamAnotacoes,cpf);
						  ba = pacienteNovo.toByteArray();//Vetor de bytes recebe a entidade paciente
						arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente ));//Vou na posição onde tem o lixo
						arq.write(ba);//Escrevo meu registro no arquivo
						v = 1;
						irParaUltimaPos = false;
						dir.inserir_chave(pacienteNovo.cpf, novoId);
					}
				}
				if(v == 0){
					arq.seek(4);
					arq.writeBoolean(false);
				}
			}
			if(irParaUltimaPos){//Se não houver lápide, vou na última posição disponível
				aux = qtdRegistros;
				++qtdRegistros;
				pacienteNovo = new Paciente(qtdRegistros,tamAnotacoes,cpf);
				  ba = pacienteNovo.toByteArray();//Vetor de bytes recebe a entidade paciente
				arq.seek(primeiraPosRegistros + (aux * bytesPorPaciente));//Vou na ultima posição 
				arq.write(ba);//Escrevo meu registro no arquivo
				arq.seek(0);//Posição com a quantidade de registros
				arq.writeInt(qtdRegistros);//Atualizo quantidade de registros no arquivo
				dir.inserir_chave(pacienteNovo.cpf, qtdRegistros);
			}
		}
	}

	private static void resgatarPaciente(RandomAccessFile arq, Diretorio1 dir, int tamAnotacoes, long bytesPorPaciente, int cpf) throws IOException {
		int y = 0;
		int endereco = dir.recuperar_endereco(cpf);
		int primeiraPosRegistros = 9;
		boolean verificaLapide;
		byte ba [];
		endereco--;
		if(endereco != -1 && endereco != -2){
			arq.seek(primeiraPosRegistros + ( endereco * bytesPorPaciente));
			verificaLapide = arq.readBoolean();
			if(verificaLapide == false){
				arq.seek(primeiraPosRegistros + ( endereco * bytesPorPaciente));//Inicio do registro
				Paciente ver = new Paciente();
				ba = new byte[(int)bytesPorPaciente];
				arq.read(ba);
				ver.fromByteArray(ba);
				y++;
			}
		}
		if(y == 0){//Se nao achar o registro
			System.out.println("=======================================");
			System.out.println("| USUÁRIO COM ESSE CPF NÃO ENCONTRADO |");
			System.out.println("=======================================");	
		} 
	}

	static void embaralharVetor(int v[]){ //Embaralha um vetor que é o parâmetro
		Random ale = ThreadLocalRandom.current();
        for(int i = v.length - 1;i > 0; i--){
            int indice = ale.nextInt(i+1);
            int e = v[indice];
            v[indice] = v[i];
            v[i] = e;
        }
	}
	
	public static void main(String[] args) throws Exception {
		File baseDados = new File("dados/pacientes.db");
		File indice = new File("dados/diretorio.db");
		double profundidadeGlobal = 1; //p
		int tamanhoBucket = 122; //n
		Diretorio1 dir = new Diretorio1(tamanhoBucket,profundidadeGlobal);
		Paciente pacienteNovo;
		Paciente ver;  

		byte ba[];//Vetor de bytes para leitura
		byte ba1[];
		RandomAccessFile arq; //Acesso aleatório
		RandomAccessFile ind;
    	Scanner input = new Scanner(System.in);//Entrada de dados
		//Cabeçalho 5 bytes iniciais do arquivo [0-4]
		int qtdRegistros;//Variável para contar quantos registros existe
		boolean existeLapide;//Variável para saber se existe lixo
		int opcao;//Variável para escoher opção do menu
		long primeiraPosRegistros = 9; // Primeira posíção depois do cabeçalho e o início dos registros
		long bytesPorPaciente;
		int tamAnotacoes; //Variável para indicar o tamanho de registros //m
		
		boolean verificaLapide;
		boolean irParaUltimaPos;
		int verificaCPF;
		int verificaID;

		int buscaCPF;
		int y = 0;
		int endereco;

		int kElementos; //k
		int Elementos [];
		long tempoAntes;
		long tempoDepois;

		try {
			if(baseDados.isFile()){//Se o arquivo já existir
       			arq = new RandomAccessFile("dados/pacientes.db","rw");//Novo objeto de acesso aleatório do arquivo mestre
				ind = new RandomAccessFile(indice, "rw");
        		arq.seek(0);//Moviemnto para a posição 0 do arquivo onde está salvo a quantidade
        		qtdRegistros = arq.readInt();//Atualizo minha variável para saber a quantidade de registros
				arq.seek(4);//Posição que indica se há lixo
				existeLapide = arq.readBoolean();//Ler e colocar na variável
				arq.seek(5);
				tamAnotacoes = arq.readInt();
				bytesPorPaciente = 27 + 2 + tamAnotacoes;

				int larguraDir = ind.readInt();
				ba1 = new byte[larguraDir];
				ind.read(ba1);
				dir.fromByteArray_diretorio(ba1);
				
			}
			else{//Novo arquivo
        		baseDados.createNewFile();
				indice.createNewFile();
				arq = new RandomAccessFile(baseDados, "rw");//Escrita
				ind = new RandomAccessFile(indice, "rw");
				arq.seek(0);//Posição 0 onde está a quantidade de registros
				arq.writeInt(0);//Colocar 0 pois ainda não existem registros
				qtdRegistros = 0;
				existeLapide = false;
				arq.seek(4);
				arq.writeBoolean(false);//Coloco false pois ainda não houve exclusão
				arq.seek(5);
				System.out.println("==================================");
				System.out.println("| DIGITE O TAMANHO DAS ANOTAÇÕES |");
				System.out.println("==================================");
				do{
				tamAnotacoes = input.nextInt();
				if(tamAnotacoes < 0){
					System.out.println("| DIGITE UM TAMANHO VÁLIDO |");
				}
				}while(tamAnotacoes < 0);
				arq.writeInt(tamAnotacoes); //Colocar o tamanho do Registro
				bytesPorPaciente = 27 + 2 + tamAnotacoes;
			}
			do{
				System.out.println("==========================================");
				System.out.println("| SISTEMA DE GERENCIAMENTO DE PACIENTES  |");
				System.out.println("==========================================");
				System.out.println("| ESCOLHA UMA DAS OPÇÕES ABAIXO :        |");
				System.out.println("| 1) CADASTRAR UM PACIENTE               |");
				System.out.println("| 2) VISUALIZAR TODOS OS PRONTUÁRIOS     |");
				System.out.println("| 3) PESQUISAR UM PACIENTE               |");
				System.out.println("| 4) EXCLUIR UM PACIENTE                 |");
				System.out.println("| 5) ALTERAR DADOS DE UM PACIENTE        |");
				System.out.println("| 6) REDEFINIR TAMANHO DO RELATORIO*     |");
				System.out.println("| 7) REINICIAR SISTEMA*                  |");
				System.out.println("| 8) TESTAR SISTEMA*                     |");
				System.out.println("| 9) CRIADORES DO SISTEMA                |");
				System.out.println("| 0) SAIR DO SISTEMA                     |");
				System.out.println("==========================================");
				System.out.println("| *) SE EXECUTAR, IRÁ FORMATAR O SISTEMA |");
				System.out.println("==========================================");
				opcao = input.nextInt();

				//Função -> (++qtd*100) + 5
				
				switch(opcao){
					case 1: //Inserir um PACIENTE
						int v = 0;
						int aux;
						arq.seek(0);
						qtdRegistros = arq.readInt();//Leio a quantidade de registros
						if(qtdRegistros == 0){//Não existe PACIENTE
							++qtdRegistros;
							pacienteNovo = new Paciente(qtdRegistros,tamAnotacoes);
              				ba = pacienteNovo.toByteArray();//Vetor de bytes recebe a entidade paciente
							arq.seek(primeiraPosRegistros);//Primeira posição de escrita
							arq.write(ba);//Escrevo meu registro no arquivo
							arq.seek(0);//Posição com a quantidade de registros
							arq.writeInt(qtdRegistros);//Atualizo quantidade de registros no arquivo
							dir.inserir_chave(pacienteNovo.cpf, qtdRegistros);
						}
						else{//existe PACIENTE
							arq.seek(4);
							existeLapide = arq.readBoolean();
							irParaUltimaPos = true;
							if(existeLapide == true){//Procurar primeiro registro lixo
								for(int i = 0; i < qtdRegistros && v != 1 ; i++){
									arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente ) );//Verifico cada registro
									verificaLapide = arq.readBoolean();
									if(verificaLapide == true){
										int x = qtdRegistros;//Nao atualizo a quantidade de registros pois eh uma lapide
										arq.seek(primeiraPosRegistros + 1 + (i * bytesPorPaciente ) );
										int novoId = arq.readInt();
										pacienteNovo = new Paciente(novoId,tamAnotacoes);
              							ba = pacienteNovo.toByteArray();//Vetor de bytes recebe a entidade paciente
										arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente ));//Vou na posição onde tem o lixo
										arq.write(ba);//Escrevo meu registro no arquivo
										v = 1;
										irParaUltimaPos = false;
										dir.inserir_chave(pacienteNovo.cpf, novoId);
									}
								}
								if(v == 0){
									arq.seek(4);
									arq.writeBoolean(false);
								}
							}
							if(irParaUltimaPos){//Se não houver lápide, vou na última posição disponível
								aux = qtdRegistros;
								++qtdRegistros;
								pacienteNovo = new Paciente(qtdRegistros,tamAnotacoes);
              					ba = pacienteNovo.toByteArray();//Vetor de bytes recebe a entidade paciente
								arq.seek(primeiraPosRegistros + (aux * bytesPorPaciente));//Vou na ultima posição 
								arq.write(ba);//Escrevo meu registro no arquivo
								arq.seek(0);//Posição com a quantidade de registros
								arq.writeInt(qtdRegistros);//Atualizo quantidade de registros no arquivo
								dir.inserir_chave(pacienteNovo.cpf, qtdRegistros);
							}
						}
					break;
					case 2: //Visualização
						arq.seek(0);
						qtdRegistros = arq.readInt();  
						for(int i = 0 ; i < qtdRegistros ; i++ ){
							arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente));
							verificaLapide = arq.readBoolean();
							arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente));
							if(verificaLapide == false){
								ver = new Paciente();
								ba = new byte[(int)bytesPorPaciente];
								arq.read(ba);
								ver.fromByteArray(ba); 
								System.out.println(ver);
							}
						}
					break;
					case 3: //Pesquisa por paciente
						y = 0;
						System.out.println("==============================");
						System.out.println("| DIGITE O CPF PARA A BUSCA: |");
						System.out.println("==============================");
						buscaCPF = input.nextInt();//Usuario digita CPF
						endereco = dir.recuperar_endereco(buscaCPF);
						endereco--;
						if(endereco != -1 && endereco != -2){
							arq.seek(primeiraPosRegistros + ( endereco * bytesPorPaciente));
							verificaLapide = arq.readBoolean();
							if(verificaLapide == false){
								arq.seek(primeiraPosRegistros + ( endereco * bytesPorPaciente));//Inicio do registro
								ver = new Paciente();
								ba = new byte[(int)bytesPorPaciente];
								arq.read(ba);
								ver.fromByteArray(ba);
								System.out.println(ver);//Imprimo os dados do paciente
								y++;
							}
						}

						
						/* for(int i = 0 ; i < qtdRegistros && y != 1 ; i++ ){
							arq.seek(primeiraPosRegistros + 5 + ( i * bytesPorPaciente));//Vou no CPF de cada registro
							verificaCPF = arq.readInt();//Leio o CPF
							if(buscaCPF == verificaCPF){//Se true, achou
								arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente));//Verificar se nao e lixo
								verificaLapide = arq.readBoolean();//Leio a lapide
								if(verificaLapide == false){
									arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente));//Inicio do registro
									ver = new Paciente();
									ba = new byte[(int)bytesPorPaciente];
									arq.read(ba);
									ver.fromByteArray(ba);
									System.out.println(ver);//Imprimo os dados do paciente
									y++;
								}
							}
						}*/

						if(y == 0){//Se nao achar o registro
							System.out.println("=======================================");
							System.out.println("| USUÁRIO COM ESSE CPF NÃO ENCONTRADO |");
							System.out.println("=======================================");	
						} 
					break;
					case 4: //Excluir um paciente
						y = 0;
						System.out.println("=================================");
						System.out.println("| DIGITE O CPF PARA A EXCLUSÃO: |");
						System.out.println("=================================");
						buscaCPF = input.nextInt();//Usuario digita CPF
						endereco = dir.recuperar_endereco(buscaCPF);
						endereco--;
						if(endereco != -1){
							arq.seek(primeiraPosRegistros + ( endereco * bytesPorPaciente));
							verificaLapide = arq.readBoolean();
							if(verificaLapide == false){
								arq.seek(primeiraPosRegistros + ( endereco * bytesPorPaciente));//Vou na posicao lapide
								arq.writeBoolean(true);//Escrevo true como existeLapide
								arq.seek(4); //Posição da verificação de exitencia de lapide
								arq.writeBoolean(true);
								y++;
								dir.remover_chave(buscaCPF);
							}
						}
					

						/* for(int i = 0 ;i < qtdRegistros && y != 1; i++ ){
							arq.seek(primeiraPosRegistros + 5 + ( i * bytesPorPaciente));//Vou no CPF de cada registro
							verificaCPF = arq.readInt();//Leio o CPF
							if(buscaCPF == verificaCPF){//Se true, achou
								arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente));//Vou na posicao lapide
								arq.writeBoolean(true);//Escrevo true como existeLapide
								arq.seek(4); //Posição da verificação de exitencia de lapide
								arq.writeBoolean(true);
								y++;
							}
						} */

						if(y == 0){//Se nao achar o registro
						System.out.println("========================================");
						System.out.println("| USUÁRIO COM ESSE CPF NÃO ENCONTRADO, |");
						System.out.println("| NÃO FOI POSSÍVEL FAZER A EXCLUSÃO    |");
						System.out.println("========================================");	
						}
					break;
					case 5: //Update
						y = 0;
						System.out.println("====================================");
						System.out.println("| DIGITE O CPF PARA A ATUALIZAÇÃO: |");
						System.out.println("====================================");
						buscaCPF = input.nextInt();//Usuario digita CPF
						endereco = dir.recuperar_endereco(buscaCPF);
						endereco--;
						if(endereco != -1){
							arq.seek(primeiraPosRegistros + ( endereco * bytesPorPaciente));
							verificaLapide = arq.readBoolean();
							if(verificaLapide = false){
								arq.seek(primeiraPosRegistros+ 1 + ( endereco * bytesPorPaciente));//Posicao com ID
								verificaID = arq.readInt();
								pacienteNovo = new Paciente(verificaID,tamAnotacoes);
								qtdRegistros++;
								  ba = pacienteNovo.toByteArray();//Vetor de bytes recebe a entidade paciente
								arq.seek(primeiraPosRegistros + ( endereco * bytesPorPaciente));
								arq.write(ba);
								y++;
								dir.remover_chave(buscaCPF);
								dir.inserir_chave(pacienteNovo.cpf, endereco);
							}
						}

						/* for(int i = 0 ; i < qtdRegistros && y != 1 ; i++ ){
							arq.seek(primeiraPosRegistros + 5 + ( i * bytesPorPaciente));//Vou no CPF de cada registro
							verificaCPF = arq.readInt();//Leio o CPF
							if(buscaCPF == verificaCPF){//Se true, achou
								arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente));//Verificar se nao e lixo
								verificaLapide = arq.readBoolean();//Leio a lapide
								if(verificaLapide == false){
									arq.seek(primeiraPosRegistros+ 1 + ( i * bytesPorPaciente));//Posicao com ID
									verificaID = arq.readInt();
									pacienteNovo = new Paciente(verificaID,tamAnotacoes);
									qtdRegistros++;
              						ba = pacienteNovo.toByteArray();//Vetor de bytes recebe a entidade paciente
									arq.seek(primeiraPosRegistros + ( i * bytesPorPaciente));
									arq.write(ba);
									y++;
								}
							}
						} */

						if(y == 0){//Se nao achar o registro
						System.out.println("========================================");
						System.out.println("| USUÁRIO COM ESSE CPF NÃO ENCONTRADO, |");
						System.out.println("| NÃO FOI POSSÍVEL FAZER A ATUALIZAÇÃO |");
						System.out.println("========================================");	
						}
					break;
					case 6: //Formtatação com mudança de tamanho de relatório
						baseDados.delete();
						indice.delete();
						baseDados.createNewFile();
						indice.createNewFile();
						arq = new RandomAccessFile(baseDados, "rw");//Escrita
						arq.seek(0);//Posição 0 onde está a quantidade de registros
						arq.writeInt(0);//Colocar 0 pois ainda não existem registros
						qtdRegistros = 0;
						existeLapide = false;
						arq.seek(4);
						arq.writeBoolean(false);//Coloco false pois ainda não houve exclusão
						arq.seek(5);
						System.out.println("==================================");
						System.out.println("| DIGITE O TAMANHO DAS ANOTAÇÕES |");
						System.out.println("==================================");
						do{
							tamAnotacoes = input.nextInt();
							if(tamAnotacoes < 0){
								System.out.println("| DIGITE UM TAMANHO VÁLIDO |");
							}
						}while(tamAnotacoes < 0);
						arq.writeInt(tamAnotacoes); //Colocar o tamanho do Registro
						bytesPorPaciente = 27 + 2 + tamAnotacoes;
						dir = new Diretorio1(tamanhoBucket, profundidadeGlobal);
						System.out.println("======================================");
						System.out.println("| SISTEMA REDEFINIDO, DADOS APAGADOS |");
						System.out.println("======================================");
					break;
					case 7: //Formtação sem mudança do tamanho do relatório
						baseDados.delete();
						indice.delete();
						baseDados.createNewFile();
						indice.createNewFile();
						arq = new RandomAccessFile(baseDados, "rw");//Escrita
						arq.seek(0);//Posição 0 onde está a quantidade de registros
						arq.writeInt(0);//Colocar 0 pois ainda não existem registros
						qtdRegistros = 0;
						existeLapide = false;
						arq.seek(4);
						arq.writeBoolean(false);//Coloco false pois ainda não houve exclusão
						arq.seek(5);
						arq.writeInt(tamAnotacoes);
						bytesPorPaciente = 27 + 2 + tamAnotacoes;
						dir = new Diretorio1(tamanhoBucket, profundidadeGlobal);
						System.out.println("======================================");
						System.out.println("| SISTEMA REDEFINIDO, DADOS APAGADOS |");
						System.out.println("======================================");
					break;
					case 8: //Executar testes
						System.out.println("========================================");
						System.out.println("| DIGITE O NÚMERO DE CHAVES PARA TESTE |");
						System.out.println("========================================");
						kElementos = input.nextInt();
						Elementos = IntStream.rangeClosed(1, kElementos).toArray();
						System.out.println(Elementos[0]);
						embaralharVetor(Elementos);
						if(qtdRegistros == 0){
							tempoAntes = System.currentTimeMillis();
							for(int i = 0;i < Elementos.length; i++){
								inserirPaciente(arq, dir, tamAnotacoes, bytesPorPaciente, Elementos[i]);
							}
							tempoDepois = System.currentTimeMillis();

							System.out.println("============================================================");
							System.out.println("| RESULTADO:                                               |");
							System.out.println("|----------------------------------------------------------|");
							System.out.println("| " + (tempoDepois - tempoAntes) + " milisegundos");
							System.out.println("============================================================");

							System.out.println("=========================================");
							System.out.println("| DESEJA EXECUTAR O TESTE DE PESQUISA ? |");
							System.out.println("| 11) SIM | QUALQUER OUTRO NÚMERO) NÃO   |");
							System.out.println("=========================================");
							int opcao1 = input.nextInt();
							embaralharVetor(Elementos);
							if(opcao1 == 11){
								tempoAntes = System.currentTimeMillis();
								for(int i = 0;i < Elementos.length; i++){
									resgatarPaciente(arq, dir, tamAnotacoes, bytesPorPaciente, Elementos[i]);
								}
								tempoDepois = System.currentTimeMillis();
	
								System.out.println("============================================================");
								System.out.println("| RESULTADO:                                               |");
								System.out.println("|----------------------------------------------------------|");
								System.out.println("| " + (tempoDepois - tempoAntes) + " milisegundos");
								System.out.println("============================================================");
							}

							baseDados.delete();
							indice.delete();
							baseDados.createNewFile();
							indice.createNewFile();
							arq = new RandomAccessFile(baseDados, "rw");//Escrita
							arq.seek(0);//Posição 0 onde está a quantidade de registros
							arq.writeInt(0);//Colocar 0 pois ainda não existem registros
							qtdRegistros = 0;
							existeLapide = false;
							arq.seek(4);
							arq.writeBoolean(false);//Coloco false pois ainda não houve exclusão
							arq.seek(5);
							arq.writeInt(tamAnotacoes);
							bytesPorPaciente = 27 + 2 + tamAnotacoes;
							dir = new Diretorio1(tamanhoBucket, profundidadeGlobal);
							System.out.println("======================================");
							System.out.println("| SISTEMA REDEFINIDO, DADOS APAGADOS |");
							System.out.println("======================================");

						}else{
							System.out.println("============================================================");
							System.out.println("| O SISTEMA PRECISA SER FORMATADO ANTES DE REALIZAR TESTES |");
							System.out.println("============================================================");
						}
						break;
					case 9: //Criadores
						System.out.println("=================================");
						System.out.println("| RAFAEL BRANDÃO NUNES - 680480 |");
						System.out.println("=================================");
						System.out.println("| LUCAS DANILO - 698961         |");
						System.out.println("=================================");		
					break;
					case 0: //Saída
						System.out.println("=============================");
						System.out.println("| OBRIGADO, VOLTE SEMPRE :) |");
						System.out.println("=============================");
					break;
					default: // Digitou incorretamente
						System.out.println("====================================");
						System.out.println("| OPÇÃO INVÁLIDA, TENTE NOVAMENTE! |");
						System.out.println("====================================");
					break;
			}
		}while(opcao != 0);

		
		ba1 = dir.toByteArray_diretorio();
		ind.writeInt(ba1.length);
		ind.write(ba1);
		arq.close();
		ind.close();
		input.close();

		} catch (IOException e) {
			 e.printStackTrace();
		}
	}

}

//Hash Extensível
class Diretorio1 {

    private File bucketsDB; //Administrador do arquivo de cestos
    private RandomAccessFile arc; //Manipulador de bytes de arquivo
    public double profundidade_global; //Define o tamanho do diretório
    private int [] copia_diretorio; //Vetor para fins de expansão do diretório
    private int tamBucket; //Define o tamanho do bucket
    private int bytesBucket = -1;//(tamBucket * 9) + 8; //Numero de bytes que um bucket ocupa
    public int [] Diretorio; //vetor de endereços dos cestos
    

    
    //Construtor do diretório e controlador do Hash Extendido.
    //Requer o tamanho do Bucket, a profundidade global
    public Diretorio1(int tamBucket, double profundidade_global) throws IOException{
        this.profundidade_global = profundidade_global;
        this.tamBucket = tamBucket;
        Diretorio = new int[(int)Math.pow(2,profundidade_global)];
        Arrays.fill(Diretorio, -1);
        bucketsDB = new File("dados/indice.db");
        if(bucketsDB.isFile()){
            arc = new RandomAccessFile(bucketsDB, "rw");
        }else{
            bucketsDB.createNewFile();
            arc = new RandomAccessFile(bucketsDB, "rw");
        }

    }

    //Dobra o Diretório mantêndo os valores iguais entre as duas metades
    public void expandir_diretorio(){
        this.profundidade_global++;
        copia_diretorio = Diretorio;
        this.Diretorio = new int[(int)Math.pow(2,profundidade_global)];
        int contador = 0;
        for (int i = 0;i < 2; i++){
            for(int j = 1;j <= (Diretorio.length+1)/2;j++){
                this.Diretorio[contador] = copia_diretorio[j-1];
                contador++;
            }
        }
    }

    //Encontra o bucket em que os dados vão ocupar e lida com a divisão de buckets.
    //definir o CPF e o endereço do paciente, o sobreescreverBucket serve para divisão de buckets.
    private void inserir_chave(int cpf, int end,boolean sobreescreverBucket, Bucket cestoVelho) throws IOException{
        if(cpf > 0 && end > 0){
            int indiceChave = Math.floorMod(cpf, (int)Math.pow(2,profundidade_global));
            bytesBucket = (tamBucket * 9) + 8;
            byte [] ba = new byte[bytesBucket];
            Bucket cesto = new Bucket(tamBucket);

            if(sobreescreverBucket == false){
                insersao_em_bucket(cpf, end, indiceChave, ba, cesto);
            }else{
                if(Diretorio[indiceChave] != indiceChave){
                    cesto = new Bucket(tamBucket, cestoVelho.prof_local+1);
                    Diretorio[indiceChave] = indiceChave;
                    cesto.registros_cpf[0] = cpf;
                    cesto.registros_posicao[0] = end;
                    cesto.lapides[0] = false;
                    cesto.qtd_ocupadas = cesto.qtd_ocupadas + 1;
                    ba = cesto.toByteArray_bucket();
                    arc.seek((Diretorio[indiceChave]*bytesBucket)); 
                    arc.write(ba);
                }else{
                    insersao_em_bucket(cpf, end, indiceChave, ba, cesto);
                }
            }
        }else{
            System.out.println("Valores de CPF e/ou endereço inválidos !!!" + "\nCPF = " + cpf + " end = " + end);
        }
    }

    
    //Encontra o bucket em que os dados vão ocupar, apenas.
    //definir o CPF e o endereço do paciente.
    public void inserir_chave(int cpf, int end) throws IOException{
        if(cpf > 0 && end > 0){
            int indiceChave = Math.floorMod(cpf, (int)Math.pow(2,profundidade_global));
            bytesBucket = (tamBucket * 9) + 8;
            byte [] ba = new byte[bytesBucket];
            Bucket cesto = new Bucket(tamBucket);
            insersao_em_bucket(cpf, end, indiceChave, ba, cesto);
        }else{
            System.out.println("Valores de CPF e/ou endereço inválidos !!!" + "\nCPF = " + cpf + " end = " + end);
        }
    }

    //Insere os dados no bucket
    //Requer o cpf, o endereço, o indiceChave e uma instância de bucket.
    private void insersao_em_bucket(int cpf, int end, int indiceChave,byte [] ba,Bucket cesto) throws IOException{
        int contador = 0;
        boolean cessar = false; 
        
        if(Diretorio[indiceChave] != -1){
            arc.seek((Diretorio[indiceChave]*bytesBucket)); 
            arc.read(ba);
            cesto.fromByteArray_bucket(ba);
            
            for(int i = 0; i < cesto.registros_cpf.length;i++){
                int vetorNull = 0;
                vetorNull = vetorNull + cesto.registros_cpf[i];
                if(i == cesto.registros_cpf.length-1 && vetorNull == 0){
                    System.out.println("vetor null -- insercao_em_bucket -- arquivo aberto");
                    System.out.println("localização = " + (indiceChave*bytesBucket) );
                    System.out.println("indice chave = " + indiceChave);
                    System.out.println("diretorio[indiceChave] = " + Diretorio[indiceChave]);
                }
            }

            /* System.out.println("\nDiretório:\n");
            for(int i = 0; i < Diretorio.length;i++){
                System.out.println("[" + i + "] = " + Diretorio[i]);
            } */

            if( cesto.qtd_ocupadas != cesto.tam_local){
                do{
                    if(contador >= cesto.registros_cpf.length){ //Verificaçao de erro
                        System.out.println();
                        System.out.println(cpf);
                        System.out.println("length = " + cesto.registros_cpf.length);
                        System.out.println("tam_local = " + cesto.tam_local);
                        System.out.println("qtd_ocupadas = " + cesto.qtd_ocupadas);
                        for(int i = 0; i < cesto.registros_cpf.length;i++){
                            int vetorNull = 0;
                            vetorNull = vetorNull + cesto.registros_cpf[i];
                            if(i == cesto.registros_cpf.length-1 && vetorNull == 0){
                                System.out.println("vetor null -- insercao_em_bucket");
                            }
                        }
                    }
                    if(cesto.registros_cpf[contador] == -1 || cesto.lapides[contador] == true){
                        cesto.registros_cpf[contador] = cpf;
                        cesto.registros_posicao[contador] = end;
                        cesto.lapides[contador] = false;
                        cessar = true;
                    }
                    contador++;
                }while(cessar == false);

                cesto.qtd_ocupadas = cesto.qtd_ocupadas + 1;
                ba = cesto.toByteArray_bucket();
                arc.seek((Diretorio[indiceChave]*bytesBucket)); 
                arc.write(ba);

            }else{
                if(profundidade_global != cesto.prof_local){
                    limpar_bucket(indiceChave, cesto.prof_local);
                    dividir_chaves(cesto,cpf,end);
                }else{
                    limpar_bucket(indiceChave, cesto.prof_local);
                    expandir_diretorio();
                    dividir_chaves(cesto,cpf,end);
                }
            }
        }else{
            cesto = new Bucket(tamBucket, (int)profundidade_global);
            Diretorio[indiceChave] = indiceChave;
            cesto.registros_cpf[0] = cpf;
            cesto.registros_posicao[0] = end;
            cesto.lapides[0] = false;
            cesto.qtd_ocupadas = cesto.qtd_ocupadas + 1;
            ba = cesto.toByteArray_bucket();
            arc.seek((Diretorio[indiceChave]*bytesBucket)); 
            arc.write(ba);
        }
    }

    //Sinaliza um espaço livre dentro de um bucket.
    //Requêr apena o cpf como chave.
    public void remover_chave(int cpf) throws IOException{
        if(cpf > 0){
            int contador = 0;
            boolean cessar = false;
            int indiceChave = Math.floorMod(cpf, (int)Math.pow(2,profundidade_global));
            bytesBucket = (tamBucket * 9) + 8;
            byte [] ba = new byte[bytesBucket];
            Bucket cesto = new Bucket(tamBucket);

            if(Diretorio[indiceChave] != -1){
                arc.seek((Diretorio[indiceChave]*bytesBucket)); 
                arc.read(ba);
                cesto.fromByteArray_bucket(ba);

                if(cesto.qtd_ocupadas != 0){
                    int largura = cesto.registros_cpf.length;

                    while(contador < largura && cessar == false){

                        if(cesto.registros_cpf[contador] == cpf){
                            cesto.lapides[contador] = true;
                            cesto.qtd_ocupadas--;
                            ba = cesto.toByteArray_bucket();
                            arc.seek((Diretorio[indiceChave]*bytesBucket)); 
                            arc.write(ba);

                            cessar = true;
                        }
                        contador++;
                    }

                    if(cessar == false){
                        System.out.println("O CPF não está no bucket !!");
                    }
                }else{
                    System.out.println("Este bucket está vazio !!!");
                }

            }else{
                System.out.println("Não existe bucket aqui !!!");
            }
        }else{
            System.out.println("CPF inválido !!!");
        }
    }

    //Retorna um índice para localizar o pronturario dentro do arquivo principal.
    //Requer apenas o cpf como chave.
    //Retorna um endereço que pode ser -1 se o cpf naõ for encontrado, então isso deve
    //ser lidado quando esse método for chamado.
    public int recuperar_endereco(int cpf) throws IOException{
        int endereco = -1;
        if(cpf > 0){
            int contador = 0;
            boolean cessar = false;
            int indiceChave = Math.floorMod(cpf, (int)Math.pow(2,profundidade_global));
            bytesBucket = (tamBucket * 9) + 8;
            byte [] ba = new byte[bytesBucket];
            Bucket cesto = new Bucket(tamBucket);

            if(Diretorio[indiceChave] != -1){
                arc.seek((Diretorio[indiceChave]*bytesBucket));
                arc.read(ba);
                cesto.fromByteArray_bucket(ba);

                if(cesto.qtd_ocupadas != 0){
                    int largura = cesto.registros_cpf.length;
    
                    while(contador < largura && cessar == false){

                        if(cesto.registros_cpf[contador] == cpf && cesto.lapides[contador] == false){
                            endereco = cesto.registros_posicao[contador];
                            cessar = true;
                        }
                        contador++;
                    }

                    if(cessar == false){
                        System.out.println("O CPF não está no bucket !! cpf = " + cpf);
                    }
                }else{
                    System.out.println("Este bucket está vazio !!!");
                }

            }else{
                System.out.println("Não existe bucket aqui !!!");
            }
        }else{
            System.out.println("CPF inválido !!!");
        }
        return endereco;
    }

    //Divide os dados de tal forma que à reintroduzílos ao diretorio com a criação de novos buckets.
    //Requer uma instância de bucket,e o cpf e o endereço que estouraram o bucket anterior.
    private void dividir_chaves(Bucket cesto, int cpfNovo, int endNovo) throws IOException{
        int [] vetorCPF = new int[cesto.registros_cpf.length+1];
        int [] vetorEnd = new int[cesto.registros_posicao.length+1];
        
        for(int i = 0; i < cesto.registros_cpf.length+1;i++){
            if(i < cesto.registros_cpf.length){
                vetorCPF[i] = cesto.registros_cpf[i];
                vetorEnd[i] = cesto.registros_posicao[i];
            }else{
                vetorCPF[i] = cpfNovo;
                vetorEnd[i] = endNovo;
            }
        }

        for(int i = 0;i < vetorCPF.length;i++){
            inserir_chave(vetorCPF[i], vetorEnd[i], true, cesto);
        }
    }

    //Limpa um bucket para abrir espaço quando for dividir os valores entre buckets
    //Requer o indiceChave já estabelecido e um prof_local para criar um novo bucket com a
    //profundidade avançada.
    private void limpar_bucket(int indiceChave,int prof_local) throws IOException{
        Bucket cesto = new Bucket(tamBucket,prof_local+1);
        bytesBucket = (tamBucket * 9) + 8;
        byte [] ba = new byte[bytesBucket];
        ba = cesto.toByteArray_bucket();
        arc.seek((Diretorio[indiceChave]*bytesBucket)); 
        arc.write(ba);
    }


    //Escreve o diretório em um byte array para armazenamento secundário
    //com o intuito de ser usado após o término do programa.
    public byte[] toByteArray_diretorio () throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeDouble(profundidade_global);
        dos.writeInt(Diretorio.length);
        for(int i= 0;i < Diretorio.length;i++){
            dos.writeInt(Diretorio[i]);
        }
       
        return baos.toByteArray();
    }

    //lê um byte array para resgatar um novo diretório
    //ao inicializar o programa.
    public void fromByteArray_diretorio (byte [] ba) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        int novoLength;

        profundidade_global = dis.readDouble();
        novoLength = dis.readInt();
        Diretorio = new int[novoLength];
        for(int i= 0;i < Diretorio.length;i++){
           Diretorio[i] = dis.readInt();
        }

    }
}

//Bucket / Cesto
class Bucket{
    //Classe do cesto

    protected int prof_local; //prof_local varia entre buckets
    protected int tam_local;  //igual para todos os buckets
    protected int qtd_ocupadas; 
    protected boolean [] lapides;
    protected int [] registros_cpf;
    protected int [] registros_posicao;

    //A instância de um bucket não precisa ser enchido imediatamente,
    //só precisa definir o tamanho inicial.
    public Bucket(int tam){
        tam_local = tam;
        registros_cpf = new int [tam_local];
        Arrays.fill(registros_cpf, -1);
        registros_posicao = new int [tam_local];
        lapides = new boolean [tam_local];
    }

    //A isntância de um bucket para inicializar um novo bucket,
    //precisa definir tamanho inicial e profundidade local.
    public Bucket(int tam,int profundidade){
        tam_local = tam;
        prof_local = profundidade;
        registros_cpf = new int [tam_local];
        Arrays.fill(registros_cpf, -1);
        registros_posicao = new int [tam_local];
        lapides = new boolean [tam_local];
    }

    public byte[] toByteArray_bucket() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeInt(prof_local);
        dos.writeInt(qtd_ocupadas);
        for(int i = 0;i < tam_local;i++){
            dos.writeBoolean(lapides[i]);
            dos.writeInt(registros_cpf[i]);
            dos.writeInt(registros_posicao[i]);
        }

        return baos.toByteArray();
    }
      
    public void fromByteArray_bucket(byte[] ba) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        prof_local= dis.readInt();
        qtd_ocupadas = dis.readInt();
        for(int i = 0;i < tam_local;i++){
            lapides[i] = dis.readBoolean();
            registros_cpf[i] = dis.readInt();
            registros_posicao[i] = dis.readInt();
        }

    }
}

//Registros dos pacientes
class Paciente {
	protected boolean lapide; //1 Byte
	protected int id; //4 Bytes
	protected int cpf; //4 Bytes
	protected String nome; //16 Bytes + 2 Bytes(tam)
	protected String relatorio; //m Bytes + 2 Bytes(tam)
	
	//1 + 4 + 4 + 16(+2) = 27
	//71(+2) = 73 (por exemplo)
	//Total 100 bytes por registro
	
	Scanner ler = new Scanner(System.in);
  
	//Construtor
	public Paciente (int i){
	  lapide = false;//Lápide começa co mo false, já que se trata de um novo objeto
	  
	  id = i;//Uso do id como manipulação interna
  
	  System.out.println("================");
	  System.out.println("| DIGITE O CPF |");
	  System.out.println("================");
	  cpf = ler.nextInt();//Lê o inteiro
  
	  ler.nextLine(); //Para limpar o buffer
  
	  System.out.println("=================");
	  System.out.println("| DIGITE O NOME |");
	  System.out.println("=================");
	  nome = ler.nextLine();//Lê a String
	  if (nome.length() > 16){//Suporta somente até os 16 primeiros bytes
		nome = nome.substring(0,15);
	  }
	  else if(nome.length() < 16){
		int x = nome.length();
		while(x < 16){
		  nome += " ";//Adiciona espaços nos bytes que não foram utilizados
		  x ++;
		}
	  }
  
	  System.out.println("======================");
	  System.out.println("| DIGITE O RELATÓRIO |");
	  System.out.println("======================");
	  relatorio = ler.nextLine();
	  if (relatorio.length() > 71){//Suporta somente até os 71 primeiros bytes
		relatorio = relatorio.substring(0,70);
	  }
	  else if(relatorio.length() < 71){
		int y = relatorio.length();
		while(y < 71){
		  relatorio += " ";//Adiciona espaços nos bytes que não foram utilizados
		  y ++;
		}
	  }
	}
	  
  
	//Construtor com tamanho de relatório variável
	public Paciente (int i, int tam){
	  lapide = false;//Lápide começa co mo false, já que se trata de um novo objeto
	  
	  id = i;//Uso do id como manipulação interna
  
	  System.out.println("================");
	  System.out.println("| DIGITE O CPF |");
	  System.out.println("================");
	  cpf = ler.nextInt();//Lê o inteiro
  
	  ler.nextLine(); //Para limpar o buffer
  
	  System.out.println("=================");
	  System.out.println("| DIGITE O NOME |");
	  System.out.println("=================");
	  nome = ler.nextLine();//Lê a String
	  if (nome.length() > 16){//Suporta somente até os 16 primeiros bytes
		nome = nome.substring(0,15);
	  }
	  else if(nome.length() < 16){
		int x = nome.length();
		while(x < 16){
		  nome += " ";//Adiciona espaços nos bytes que não foram utilizados
		  x ++;
		}
	  }
  
	  System.out.println("======================");
	  System.out.println("| DIGITE O RELATÓRIO |");
	  System.out.println("======================");
	  relatorio = ler.nextLine();
	  if (relatorio.length() > tam){//Suporta somente até os 71 primeiros bytes
		relatorio = relatorio.substring(0,(tam-1));
	  }
	  else if(relatorio.length() < tam){
		int y = relatorio.length();
		while(y < tam){
		  relatorio += " ";//Adiciona espaços nos bytes que não foram utilizados
		  y ++;
		}
	  }
	}
  
	//Construtor para testes
	public Paciente (int i, int tam,int cpfEntrada){
	  lapide = false;//Lápide começa co mo false, já que se trata de um novo objeto
	  
	  id = i;//Uso do id como manipulação interna
  
	  cpf = cpfEntrada;//CPF como parametro
  
	  nome = " ";
	  if (nome.length() > 16){//Suporta somente até os 16 primeiros bytes
		nome = nome.substring(0,15);
	  }
	  else if(nome.length() < 16){
		int x = nome.length();
		while(x < 16){
		  nome += " ";//Adiciona espaços nos bytes que não foram utilizados
		  x ++;
		}
	  }
  
	  relatorio = " ";
	  if (relatorio.length() > tam){//Suporta somente até os 71 primeiros bytes
		relatorio = relatorio.substring(0,(tam-1));
	  }
	  else if(relatorio.length() < tam){
		int y = relatorio.length();
		while(y < tam){
		  relatorio += " ";//Adiciona espaços nos bytes que não foram utilizados
		  y ++;
		}
	  }
	}
  
	//Construtor sem atributos, utilizado no momento de recuperar os dados no disco
	public Paciente (){
		lapide = false;
		id = -1;
		cpf = -1;
		nome = "";
		relatorio = "";
		}
	
  
	public String toString(){
		return "\nID: " + id +
				"\nCPF: " + cpf +
				"\nNome: " + nome +
				"\nRelatório: " + relatorio;
	}
  
	//Escrita
	public byte[] toByteArray () throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		dos.writeBoolean(lapide);
		dos.writeInt(id);
		dos.writeInt(cpf);
		dos.writeUTF(nome);
		dos.writeUTF(relatorio);
  
		return baos.toByteArray();
	}
	
	//Leitura
	public void fromByteArray(byte[] ba) throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(ba);
		DataInputStream dis = new DataInputStream(bais);
  
		lapide = dis.readBoolean();
		id = dis.readInt();
		cpf = dis.readInt();
		nome = dis.readUTF();
		relatorio = dis.readUTF();
	}
}

