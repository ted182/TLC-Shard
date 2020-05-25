##############################################################################################
##############################################################################################
##############################################################################################
#O MACRO ANDA POR UMA ROTA DEFINIDA NA VARIAVEL ARVORES TIRANDO LOGS, QUANDO ATINGE O PESOMAX RECALA PRO BANCO E GUARDA TUDO
#O MACRO FOI FEITO PRA ATUAR DENTRO DA PROT, ENTÃO ELE FICA CHAMANDO GUARDS NAS ROTINAS
#SE COLOCAR BANDAGEM E LIFEBOOST NA BAG, ELE USA QND FOR ATACADO
#NÃO INICIAR O MACRO NO LOCAL (coords XY) ONDE RECALA PRO BANCO
#configurar variaveis de cada char antes de iniciar (aproximadamente linha 31)
#inserir array de arvores antes de dar o play (aproximadamente linha 80) (macro de EASYUO disponivel na mesma pasta que este arquivo)
##############################################################################################
##############################################################################################
##############################################################################################





from stealth import *
from datetime import datetime, timedelta
from random import randint, shuffle
import timeit
import time
import math



##############################################################################################
##############################################################################################
##############################################################################################
#VARIAVEIS PESSOAIS DE CADA CHAR####################################
nome = CharName()
if (nome == 'AstroZombiE'):
    print ("Script iniciado pelo char %s" %nome)
    RuneBank = 1 #Posicao da runa do banco (OBRIGATORIAMENTE TEM QUE ESTAR NA PAGINA 1)
    RuneLumberRota = 19 #Posicao da runa da rota (ARVORE Nº1) a ser seguida (OBRIGATORIAMENTE TEM QUE ESTAR NA PAGINA 1)
    RestockContainer = 0x4001780C
    LogsContainer = 0x4001780C #Bag onde vou dropar Logs

if (nome == 'SEU CHAR'):
    #adicionar seus chars aqui ...


##############################################################################################
##############################################################################################
####################################################################
# tt -> target tile
# tx -> target x
# ty -> target y
# tz -> target z
####################################################################

OresCount = {}
OreDict = { 0x0000: 'Log', 
            0x0755: 'Hard Maple', 
            0x0723: 'Hickory', 
            0x064E: 'Mahogany', 
            0x0455: 'Dark',
            0x0736: 'Cedar'}


PesoMax = 155
#MachadoType = 0x0F43
MachadoType = 0x0F47 #machado +10
bandagesType = 0x0E21
#LogTypes = [0x1BDD, 0x1BDD]
LogTypes = [0x1BDD]
BracerLumberType = 0x1086
FlagMachadoQuebrado = 0 #flag para sinalizar que o machado quebrou
ContaBuffer = 0
NumVoltas = 0
NumRetPeso = 0
BP = 0x0F7A
BM = 0x0F7B
MR = 0x0F86

#-----------------------------------------------------------		
#---------------------------------------POSIÇÕES DAS ARVORES
#-----------------------------------------------------------
#arvores[0] = [x, y, tx, ty, tt]
arvores = [
#...ADICIONAR ITENS DA ARRAY DAQUI PRA BAIXO
#[0, 0, 0, 0, 0],
#[0, 0, 0, 0, 0],
#[0, 0, 0, 0, 0],
#[0, 0, 0, 0, 0],
#[0, 0, 0, 0, 0],
#...ADICIONAR SUA ROTA AQUI (OLHAR NO REPOSITORIO UM MACRO EM EASYUO QUE FACILITA A MARCAÇÃO DAS ARVORES)
#[0, 0, 0, 0, 0],
#[0, 0, 0, 0, 0],
#...ADICIONAR ITENS DA ARRAY até aqui
[0, 0, 0, 0, 0] #A ULTIMA POSIÇÃO NÃO TEM ,(VIRGULA)

]   # <-- NAO RETIRAR ESSE "]"

#-----------------------------------------------------------		
#-------------------------------FIM DAS POSIÇÕES DAS ARVORES
#-----------------------------------------------------------

def cortaARVORE(tt,tx,ty,tz):
    #global FlagMachadoQuebrado
    ClearJournal()    
    delayDetect = 0
    delayguards = 0
    FlagMachadoQuebradoLOCAL = 0
    tentativas = 0
    if tt == 0: #verifica se o personagem apenas está andando, sem necessidade de cortar arvores
        return(FlagMachadoQuebradoLOCAL)
    while not Dead():
        timeout = time.time()
        heal()
        start = datetime.now()
        end = datetime.now() + timedelta(seconds=30)
        if (ObjAtLayerEx(LhandLayer(),Self()) or FindType(MachadoType, Backpack())) > 0:
            WaitTargetTile(tt, tx, ty, tz)
            UseType(MachadoType, -1)
            while not InJournalBetweenTimes('You put|You hack', start, end)> 0:
                heal()
                if (time.time() - delayguards) > 7:
                    delayguards = time.time()
                    UOSay('guards')
                if WarMode():
                    correGritando()
                    SetWarMode(False)
                    Wait(300)
                    return(FlagMachadoQuebradoLOCAL)
                Wait(300)
                if InJournalBetweenTimes('That is|There is|reach', start, end) > 0:                       
                    return(FlagMachadoQuebradoLOCAL)
                if (time.time() - timeout) > 29: #se o tempo de log em uma arvore for maior que 30 segundos
                    CancelTarget()
                    print('mudando arvore por timeout!')
                    return(FlagMachadoQuebradoLOCAL)
            tentativas = tentativas + 1
            if tentativas > 1000:
                break
            CheckLag(10000)
        else:
            FlagMachadoQuebradoLOCAL = 1
            print ('Reiniciando por machado quebrado')
            Wait(1000)
            #UOSay('Else do CortaArvore')
            #break
            recall(RuneBank)
            unload()
            restock()
            recall(RuneLumberRota)
            #move para a posição 1 da rota
            #NewMoveXY(arvores[1][0],arvores[1][1],True,0,True)
            return(FlagMachadoQuebradoLOCAL)
    return(FlagMachadoQuebradoLOCAL)
                        
def heal():
    if GetHP(Self()) < GetMaxHP(Self()):
        #var01 = (FindTypeEx(0x0F0E,0x0099,Backpack(),False) ) #greater heal
        var01 = (FindTypeEx(0x0F82,0x0026,Backpack(),True) ) #boost heal
        #var01 = FindType(0x0F82,Backpack()) #boost heal
        var02 = (FindType(bandagesType, Backpack()) )
        UseObject(var01)
        Wait (250)
        UseObject(var02)
        WaitTargetSelf()
        Wait (250)

def recall(runenumber):
    contador = 0
    positionX = GetX(Self())
    positionY = GetY(Self())
    while positionX == GetX(Self()) and positionY == GetY(Self()) and not Dead():
        if Mana() < 15: 
            medit(GetMaxMana(Self()))
        UOSay('.recall 1, ' + str(runenumber))
        while(contador < 10):
            UOSay('._guards')
            heal()
            Wait(1000)
            contador = contador + 1
        contador = 0
        CheckLag(10000)
        if Dead():
            break
            
def peso():
    if (Weight() > PesoMax):
        print ('Reiniciando por PESO')
        recall(RuneBank)
        UnloadedOres = unload()
        restock()
        recall(RuneLumberRota)
        
def medit(value):
    while Mana() < value and not Dead():
        if WarMode():
            SetWarMode(False)
            Wait(200)
        UseSkill('Meditation')
        Wait(2000)
        
def unload():
    logs = {} 
    UOSay('Banker bank Guards')
    Wait(500) # Somehow this is needed, otherwise will crash
    #hide()
    UseObject(LogsContainer)
    Wait(500)
    CheckLag(10000)
    for oretype in LogTypes:
        while FindType(oretype, Backpack()) > 0:
            ##########################################
            color = OreDict[GetColor(FindItem())]
            if color in logs:
                logs[color] += GetQuantity(FindItem())
            else:
                logs[color] = GetQuantity(FindItem())
            ##########################################
            MoveItem(FindItem(), GetQuantity(FindItem()), LogsContainer, 0, 0, 0)
            Wait(1000)
    return logs

def PegaBracerLumber():
    if FindType(BracerLumberType, RestockContainer) > 0:
        Wait(500)
        MoveItem(FindItem(), 1, Backpack(), 0, 0, 0)
        Wait(500)                                
    UseType(BracerLumberType, -1)
    Wait(500)

def buffer(texto):
    #verifica se existe a palavra VAZIO no buffer e retorna 1 em caso positivo
    UOSay('.buffer')
    start = datetime.now()
    end = datetime.now() + timedelta(seconds=1)
    while (InJournalBetweenTimes(texto , start, end) <= 0 and end > datetime.now()):
        Wait(300)
    linha = InJournalBetweenTimes(texto , start, end)
    conteudo = Journal(linha)
    #print (conteudo)
    conteudoFLAG = conteudo.find('vazio')
    if conteudoFLAG > 0:
        resultado = 1
    else:
        resultado = 0
    return (resultado)
    
def restock():
	# Abre o bank
	UOSay('Banker bank Guards')
	
	# Se não colocar um pause, trava a aplicação
	Wait(1000) 
	
	#Abre o bau com os reagentes e Pickaxe
	UseObject(RestockContainer)
	
	# Se não colocar um pause, trava a aplicação
	Wait(1000)
	
	#Abre a própria bag
	UseObject(Backpack())
	
	# Se não colocar um pause, trava a aplicação
	Wait(1000)
	
	# ===========================
	# Procura por BP na mochila
	# ===========================
	
	FindType(BP, Backpack())
	Wait(500)
	if GetQuantity(FindItem()) < 8:
		if FindType(BP, RestockContainer) > 0:
			Wait(500)
			MoveItem(FindItem(), 8, Backpack(), 0, 0, 0)
			Wait(500)
		Wait(500)
	
	# ===========================
	# Procura por BM na mochila
	# ===========================
	FindType(BM, Backpack())
	Wait(500)
	if GetQuantity(FindItem()) < 8:
		if FindType(BM, RestockContainer) > 0:
			Wait(500)
			MoveItem(FindItem(), 8, Backpack(), 0, 0, 0)
			Wait(500)
	Wait(500)
	
	# ===========================
	# Procura por MR na mochila
	# ===========================
	FindType(MR, Backpack())
	Wait(500)
	if GetQuantity(FindItem()) < 8:
		if FindType(MR, RestockContainer) > 0:
			Wait(500)
			MoveItem(FindItem(), 8, Backpack(), 0, 0, 0)
			Wait(500)
		Wait(500)
		
	# ===========================
	# Procura por machado na mochila
	# ===========================	
	if not (ObjAtLayerEx(RhandLayer(),Self()) or FindType(MachadoType, Backpack())) > 0:
		Wait(500)
		if FindType(MachadoType, RestockContainer) > 0:
			Wait(500)
			MoveItem(FindItem(), 1, Backpack(), 0, 0, 0)
			Wait(500)
	Wait(500)
    
            
def checaArvoresTotais():
    global arvores
    x = len(arvores)
    cont = 0
    for i in range(x):
        if (arvores[i][4]) == 0:
            cont = cont + 1
    resultado = x - cont
    return (resultado)
 
def pegarValorSKILL(skill):
    a = GetSkillValue(skill) #este valor é um double
    return (a)

def hide():
    while not Hidden() and not Dead():
        if WarMode():
            SetWarMode(False)
            Wait(300)
        UseSkill('Hiding')
        Wait(3500)

def stealth():
    while Hidden() and not Dead():
        if WarMode():
            SetWarMode(False)
            Wait(300)
        UseSkill('Stealth')
        Wait(3500) 


def tempodemacro(x,y):
    #x é o tempo de inicio do macro da variavel time.time()
    #y é o tempo atual da variavel time.time()
    horasP = 0
    minutosP = 0
    delta = y - x
    #print("delta "+ str(delta))
    
    horas = delta/3600
    minutos = delta/60

    horasP =  math.trunc(horas) #pega parte inteira
     
    if minutos < 1:
        minutosP = 0
       
    if minutos > 1:        
        if horasP < 1:
            minutosP =  math.trunc(minutos) #pega parte fracionada 
        if horasP >= 1:            
            minutosP =  math.trunc(60 * (horas % 1)) #pega parte fracionada            
    
    print("Tempo de Farm ->  " + str(horasP) + " hora(s) e " + str(minutosP) + " minuto(s)")


def correGritando():
    #x eh o numero da arvore que esta loguiando no momento
    timeout = time.time()
    while (time.time() - timeout < 15): #enquanto o tempo for menor q 10 segundos
        if WarMode():
            SetWarMode(False)
            Wait(250)
        UseSkill('Detecting Hidden')
        Wait(250)
        UOSay('._guards')
        Wait(250)
        heal()
        Wait(1250)

#-----------------------------------------------------------		
#---------------------------------------------comeca o macro
#-----------------------------------------------------------
inicio = time.time() #contar o tempo de execução do macro
#UOSay('.votar')
medit(GetMaxMana(Self()))
recall(RuneBank)
UnloadedOres = unload()
restock()
#########PegaBracerLumber()
recall(RuneLumberRota)
ContaArvores = 0
# checa quantidade de arvores
linhasAR = checaArvoresTotais()
print ("rota com o total de %i Arvores" %linhasAR)
x = 0    
while True:
#for i in range(linhas):    
    if ContaArvores == linhasAR :   
        x = 0
        ContaArvores = 0
    if FlagMachadoQuebrado == 1:
        #print ('entrou na flag machado quebrado main')
        x = 0
        FlagMachadoQuebrado = 0
        ContaArvores = 0

    if (Weight() > PesoMax):
        print ('Reiniciando por PESO')
        recall(RuneBank)
        UnloadedOres = unload()
        OresCount = { x: OresCount.get(x, 0) + UnloadedOres.get(x, 0) for x in set(OresCount) | set(UnloadedOres) }
        print(OresCount)
        restock()
        recall(RuneLumberRota)

    NewMoveXY(arvores[x+1][0],arvores[x+1][1],True,0,True)
    # não adicionar contagem quando não logiar arvore
    if (arvores[x+1][4]) != 0: 
        ContaArvores = ContaArvores + 1

    print ("Arvore %i" %ContaArvores)
    print (str(OresCount))
    tempodemacro(inicio, time.time())
    FlagMachadoQuebrado = cortaARVORE(arvores[x+1][4],arvores[x+1][2],arvores[x+1][3],0)
    x = x + 1

