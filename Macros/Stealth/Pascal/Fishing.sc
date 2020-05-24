////////////////////////////////////////////////////////
////////////////////////////////////////////////////////
//ESTE MACRO PESCA EM UMA REGIÃO DEFINIDA ENTRE XMAX-XMIN E YMAX-YMIN (UM QUADRADO OU RETANGULO)
//BUSCAR NO CÓDIGO O LOCAL ONDE AS VARIÁVEIS DE CADA CHAR DEVE SER DEFINIDA ANTES DE DAR O PLAY (proximo da linha 515) não mudar mais nada em outro local!
//NÃO INICIAR O MACRO NO LOCAL (coords XY) ONDE RECALA PRO BANCO
////////////////////////////////////////////////////////
////////////////////////////////////////////////////////


Program Fishing;

Uses 
    SysUtils;

{$Include 'all.inc'}

Const
    BP = $0F7A;
	BM = $0F7B;
	MR = $0F86;
	PeixeSpecialType = $1E1E;
	ComidaPeixeType = $097B;
	VaraType = $0DBF;
	daggerType = $0F51; 
	peixecortadoType = $097A; 
	SOSType = $099B;
	PesoMax = 350;	
	
	
Var
	coordXMIN,coordYMIN,coordXMAX,coordYMAX,TempoInicial: integer;
	RestockContainer,ChaveBarcoID,FishContainer,SOSconteinerID,magicalFishBagID,peixeCortadoBagID: cardinal;
	//TempoInicial: TDateTime;
	RuneBank,nome: string;
	peixeTypes: array[1..4] of word;
	PeixeSpecialColorTypes: array[1..2] of word;
	
	
////////////////////////////////////////////////////////
////////////////////////////////////////////////////////
////////////////////////////////////////////////////////
////////////////////////////////////////////////////////


procedure tempodemacro(tempo: integer);
{
tempo é o tempo de inicio do macro da variavel GetTickCount
trunc (trunca um valor real para um valor inteiro);
frac (retorna a parte fracionária de um número real);
round (retorna o valor real arredondado para o número inteiro mais próximo).
}
var
	delta: integer;
	horas,horasINT,minutos,minutosINT,segundos: real;	
begin
	delta := GetTickCount - tempo;
	//delta := 3888*1000; /usado pra simular os segundos de tick
	segundos := round(trunc(delta)/1000);
	horas := segundos/3600;	
	minutos := trunc(segundos/60);	
	horasINT := trunc(segundos/3600);	
	if minutos < 1 then
		minutosINT := 0;		
	if minutos > 1 then
	begin
		if horasINT < 1 then
			minutosINT := trunc(minutos);
		if horasINT >= 1 then
			minutosINT := trunc(60*(horas - horasINT));
	end;	
	//AddToSystemJournal('tempo de macro -> horas: ' + intToStr(round(horasINT)) + ' minutos: ' + intToStr(round(minutosINT)));
	//SetGlobal('stealth','TestVar2',' Horas: ' + intToStr(round(horasINT)) + ' min: ' + intToStr(round(minutosINT)));
	AddToSystemJournal('Tempo de Macro -> horas: ' + intToStr(round(horasINT)) + ' minutos: ' + intToStr(round(minutosINT)));
end;


procedure arrumabarco;
var 
a,b: integer;
daggerOBJ: cardinal;
begin
	//movendo os magic fish pra bag
    for a := 1 to high(PeixeSpecialColorTypes) do
	begin
		while (FindTypeEx(PeixeSpecialType, PeixeSpecialColorTypes[a], backpack, False) > 0) do
		begin
			MoveItem(FindItem, GetQuantity(FindItem), magicalFishBagID, 0, 0, 0)
			wait(1000)
		end;
	end;
	//adicionado rotina para mover os SOS para mesma bag dos magic fish
    while (FindType(SOSType, backpack) > 0) do
	begin
		MoveItem(FindItem, 1, magicalFishBagID, 0, 0, 0);
		wait(1000);
	end;
	//cortando os peixes
	checksave;
	waitconnection(3000);
	UOSay('.disarm');
	wait(1000);		
	if (FindType(daggerType, backpack) > 0) then begin
		daggerOBJ := FindItem;
    end
	else begin
        daggerOBJ := 0;
		AddToSystemJournal('****colocar dagger na bag****');
		PauseCurrentScript;
	end;	
	for b := 1 to high(peixeTypes) do
	begin
        //AddToSystemJournal(inttostr(b));
		while FindType(peixeTypes[b], FishContainer) > 0 do
        begin
            //AddToSystemJournal(inttostr(b));
			UseObject(daggerOBJ);
            WaitForTarget(5000);
            TargetToObject(FindItem);
            wait(1000);	
		end;
	end;	
	//stakando os peixes cortados
	while FindType(peixecortadoType, FishContainer) > 0 do
    begin
		MoveItem(FindItem, GetQuantity(FindItem), peixeCortadoBagID, 0, 0, 0);
        wait(1000);
	end;
	
end;


procedure guardaSOS;
begin
	UOSay('Banker bank Guards');
	wait(1000);
	UseObject(SOSconteinerID);
	wait(1000);
	UseObject(backpack);
	wait(1000);
	while (FindType(SOSType, backpack) > 0) do
	begin
		MoveItem(FindItem, 1, SOSconteinerID, 0, 0, 0);
		wait(1000);
	end;
end;



procedure restock;
begin
	UOSay('Banker bank Guards');
	wait(1000);
	UseObject(RestockContainer);
	wait(1000);
	UseObject(backpack);
	wait(1000);
	// ===========================
    // Procura por BP na mochila
    // ===========================
    if (GetQuantity(FindType(BP, backpack)) < 5) then
	begin
        if (FindType(BP, RestockContainer) > 0) then
		begin
            while (GetQuantity(FindType(BP, backpack)) < 5) do
			begin
				Wait(500);
				MoveItem(FindType(BP, RestockContainer), 5, backpack, 0, 0, 0);
				Wait(500);
			end;
		end
		else
		begin
			AddToSystemJournal('****ACABOU BP****');
			PauseCurrentScript;
		end;
        Wait(500);
	end;
	// ===========================
    // Procura por BM na mochila
    // ===========================
    if (GetQuantity(FindType(BM, backpack)) < 5) then
	begin
        if (FindType(BM, RestockContainer) > 0) then
		begin
            while (GetQuantity(FindType(BM, backpack)) < 5) do
			begin
				Wait(500);
				MoveItem(FindType(BM, RestockContainer), 5, backpack, 0, 0, 0);
				Wait(500);
			end;
		end
		else
		begin
			AddToSystemJournal('****ACABOU BM****');
			PauseCurrentScript;
		end;
        Wait(500);
	end;
	// ===========================
    // Procura por MR na mochila
    // ===========================
    if (GetQuantity(FindType(MR, backpack)) < 5) then
	begin
        if (FindType(MR, RestockContainer) > 0) then
		begin
            while (GetQuantity(FindType(MR, backpack)) < 5) do
			begin
				Wait(500);
				MoveItem(FindType(MR, RestockContainer), 5, backpack, 0, 0, 0);
				Wait(500);
			end;
		end
		else
		begin
			AddToSystemJournal('****ACABOU MR****');
			PauseCurrentScript;
		end;
        Wait(500);
	end;
	// ===========================
    // Procura por peixe frito na mochila
    // ===========================
    if (GetQuantity(FindType(ComidaPeixeType, backpack)) < 5) then
	begin
        if (FindType(ComidaPeixeType, RestockContainer) > 0) then
		begin
            while (GetQuantity(FindType(ComidaPeixeType, backpack)) < 5) do
			begin
				Wait(500);
				MoveItem(FindType(ComidaPeixeType, RestockContainer), 20, backpack, 0, 0, 0);
				Wait(500);
			end;
		end
		else
		begin
			AddToSystemJournal('****sem peixe frito no banco****');
		end;
        Wait(500);
	end;
	// ===========================
    // Procura por VARA na mochila e banco
    // ===========================
	if not (ObjAtLayerEx(LhandLayer,Self) or ObjAtLayerEx(RhandLayer,Self) or FindType(VaraType, backpack) > 0) then
	begin
		if FindType(VaraType, RestockContainer) > 0 then
			wait(500);
			MoveItem(FindItem, 1, backpack, 0, 0, 0);
			wait(500); 		
	end;
	Wait(500);	
	guardaSOS;
	
end;


procedure medit(value: integer);
begin
	while (mana < value) and not dead do
	begin	
		if (warmode) then
		begin
			setwarmode(false);
			wait(200);
		end;
		useskill('Meditation');
		wait(2000);
	end;
end;


procedure recall(runa: string);
var
positionX,positionY: integer;
begin
	CheckLag(10000);
	positionX := GetX(self);
    positionY := GetY(self);
	while (positionX = GetX(self)) and (positionY = GetY(self)) and (not dead) do
	begin
		if (mana < 20) then
			medit(GetMaxMana(self));
		UOSay('.recall ' + runa);
		wait(10000);
		CheckLag(10000);		
	end;
end;

procedure recallCHAVE(IDchave: cardinal);
var
positionX,positionY: integer;
begin
	CheckLag(10000);
	positionX := GetX(self);
    positionY := GetY(self);
	while (positionX = GetX(self)) and (positionY = GetY(self)) and (not dead) do
	begin
		if (mana < 20) then
			medit(GetMaxMana(self));
		cast('Recall');
		WaitTargetObject(IDchave);
		wait(10000);
		CheckLag(10000);		
	end;
end;



procedure pescar;
var 
a,x,y,ex,timeout: integer;
start : TDateTime;
begin
	for x := -4 to 4 do
	begin
		for y := -4 to 4 do
		begin
			while True do
			begin
				if ObjAtLayerEx(LhandLayer,Self) or ObjAtLayerEx(RhandLayer,Self) or FindType(VaraType, backpack) > 0 then
				begin
					timeout := GetTickCount;
					ClearJournal;
					start := Now;
					UseType(VaraType, -1);
					WaitForTarget(5000);
					TargetToXYZ( GetX(self) + x , GetY(self)+ y ,GetZ(self));
					WaitJournalLine( start, 'no fish here|You fish a while|location|far away|in water|You pull|elsewhere|found|perform|seem to be biting here', 6000); 
					ex := InJournalBetweenTimes('You pull|no fish here|location|far away|in water|elsewhere|Target cannot|seem to be biting here', start, now );
					if (weight > PesoMax) then
					begin
						UseObject(FishContainer);
						for a := 1 to high(peixeTypes) do
						begin
							while FindType(peixeTypes[a], backpack) > 0 do
							begin
								MoveItem(findItem, GetQuantity(findItem), FishContainer, 0, 0, 0);
								wait(1000);
							end;
						end;
						arrumabarco;	
						wait(500);										
						UseObject(magicalFishBagID);
						wait(500);
						FindTypeEx(PeixeSpecialType, PeixeSpecialColorTypes[1], magicalFishBagID, False);
						AddToSystemJournal('Magical Fish: ' + inttostr(GetQuantity(FindItem)));
						FindTypeEx(PeixeSpecialType, PeixeSpecialColorTypes[2], magicalFishBagID, False);
						AddToSystemJournal('Golden Fish: ' + inttostr(GetQuantity(FindItem)));
						wait(500);
						FindType(SOSType, backpack);
						AddToSystemJournal('SOS Bottle: ' + IntToStr(FindCount));
						//AddToSystemJournal('Tempo de macro: ' + TimeToStr(now - TempoInicial));
						tempodemacro(TempoInicial);
						//AddToSystemJournal(inttostr(TimeGetTime - TempoInicial));
						//tempodemacro(inicio, time.time());
						AddToSystemJournal('****************************************');
						wait(500);					
					end;				
					CheckLag(10000)
					if (GetTickCount - timeout > 60000) then
						AddToSystemJournal('timeout!');
						break;
					if (targetpresent) then
						canceltarget;
					if (ex >= 0) then
						break;
				end
				else
				begin
					AddToSystemJournal('Sem Vara de PEsca na BAg!!!!!!');
					recall(RuneBank);	
					restock;
					recallCHAVE(ChaveBarcoID);					
					NewMoveXY(GetX(FishContainer),GetY(FishContainer),True,2,True); //anda para posição de pesca dentro do barco dependendo da direção que o barco está apontando
					wait(2000);
				end;	
			end;//fim do while
		end;//fim for y
	end;//fim for x
end;

procedure andarPraFrente(tempo: integer);
//var
//y: integer;
begin
	{y := GetY(self);
	while (y = GetY(self)) do
	begin
		UOSay('foreward');
		wait(tempo);
		 if (y = GetY(self)) then
		begin
			UOSay('back');
			Wait(1000);
			UOSay('stop');
			Wait(1000);
			UOSay('right');
			Wait(1000);	
		end; 
	end; }
	UOSay('foreward');
	wait(tempo);
	UOSay('stop');
end;

procedure checaBORDAS;
var
direcao: cardinal;
begin	
	direcao := getdirection(self); 
	
	if direcao = 0 then
	begin
	//verifica se atingiu o limite Y "superior"
		if (GetY(self) < coordYMIN) then
		begin
			AddToSystemJournal('Ymin atingido!');
			while (GetY(self) < coordYMIN) do
			begin	
				UOSay('back');
				wait(3000);
				UOSay('stop');
				wait(2000);
			end;
			UOSay('turn left');
		end;
	end; // end direção norte
	
	if direcao = 6 then
	begin
	//verifica se atingiu o limite Y "inferior"
		if (GetX(self) < coordXMIN) then
		begin
			AddToSystemJournal('Xmin atingido!');
			while (GetX(self) < coordXMIN) do
			begin	
				UOSay('back');
				wait(3000);
				UOSay('stop');
				wait(2000);
			end;
			UOSay('turn left');
		end;
	end;// end direção oeste
	
	if direcao = 4 then
	begin
		//verifica se atingiu o limite X superior
		if (GetY(self) > coordYMAX) then	
		begin
			AddToSystemJournal('Ymax atingido!');
			while (GetY(self) > coordYMAX) do
			begin	
				UOSay('back');
				wait(3000);
				UOSay('stop');
				wait(2000);
			end;
			UOSay('turn left');
		end;
	end;// end direção sul
	
	if direcao = 2 then
	begin
		//verifica se atingiu o limite X minimo
		if (GetX(self) > coordXMAX) then
		begin
			AddToSystemJournal('XMIN atingido!');
			while (GetX(self) > coordXMAX) do
			begin	
				UOSay('back');
				wait(3000);
				UOSay('stop');
				wait(2000);
			end;
			UOSay('turn left');
		end;
	end;// end direção leste
end;





















    







////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
begin
	//*****************************************************************************
	//*************************DEFINI AS VARIAVEIS DE CADA CHAR********************
	//*****************************************************************************
	nome := charname;	
	if (nome = 'AstroZombiE') then 
	begin		
		RuneBank := '1,1'; //pagina 1 posição 1
		RestockContainer := $4001780C; //container do banco onde ficam reags e varas
		ChaveBarcoID := $40046A2A;
		FishContainer := $40047075; //container do barco
		SOSconteinerID := $4001938A; //essa bag fica no banco
		magicalFishBagID := $400758C5; //bag fica no bau do barco
		peixeCortadoBagID := $4007588C; //bag fica no bau do barco
		//no caso desse char o macro vai de uma coord Y grande pra uma coordY pequena
		coordXMIN := 0; //limite minimo da coord X
		coordXMAX := 0; //limite maximo da coord X
		coordYMIN := 0;  
		coordYMAX := 0; 
	end;
	
	if (nome = 'SEU CHAR') then 
	begin
		//...ADICIONAR OUTROS CHAR SEGUINDO ESTA LÓGICA DO ANTERIOR (NÃO PULE VARIAVEIS)
	end;
	
	
	
	
	
	
	//*****************************************************************************
	//**********************DEFININDO VETORES FIXOS********************************
	//*****************************************************************************
	peixeTypes[1] := $09CC;  //PEIXE VERDE
	peixeTypes[2] := $09CD;  //PEIXE MARROM
	peixeTypes[3] := $09CF;  //PEIXE AMARELO
	peixeTypes[4] := $09CE;  //PEIXE AZUL
	
	PeixeSpecialColorTypes[1] := $0AB4 //magical
	PeixeSpecialColorTypes[2] := $045E //golden
	//*****************************************************************************
	//*****************************************************************************
	//*****************************************************************************
		
	//turn around -> muda o barco 180°
	//turn left -> barco vira 90° pra esquerda
	AddToSystemJournal('Script iniciado pelo char: ' + nome);
	SetPauseScriptOnDisconnectStatus(True);
	SetARStatus(True);
	//TempoInicial := now;	
	TempoInicial := GetTickCount;
	medit(GetMaxMana(self));
	recall(RuneBank);	
	restock;
	recallCHAVE(ChaveBarcoID);	
	
	NewMoveXY(GetX(FishContainer),GetY(FishContainer),True,2,True); //anda para posição de pesca dentro do barco dependendo da direção que o barco está apontando
	wait(2000);
	
	while True do
	begin		
		//procura comida na bag e come
		if (FindType(ComidaPeixeType, backpack) > 0) then
			UseType(ComidaPeixeType, -1);
		pescar;
		andarPraFrente(2000);
		checaBORDAS;
	end;
end.
