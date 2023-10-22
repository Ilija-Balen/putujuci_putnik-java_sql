
CREATE TABLE [Article]
( 
	[IdArticle]          integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Price]              decimal(10,3)  NULL ,
	[Count]              integer  NULL ,
	[IdShop]             integer  NULL ,
	[Name]               char(100)  NULL 
)
go

CREATE TABLE [Buyer]
( 
	[IdBuyer]            integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdCity]             integer  NULL ,
	[Credit]             decimal(10,3)  NULL ,
	[Name]               char(100)  NULL 
)
go

CREATE TABLE [City]
( 
	[IdCity]             integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Name]               char(100)  NULL 
)
go

CREATE TABLE [Discount]
( 
	[IdDiscount]         integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdShop]             integer  NULL ,
	[Percentage]         integer  NULL 
	CONSTRAINT [Validation_Rule_357_1667534931]
		CHECK  ( Percentage BETWEEN 0 AND 100 )
)
go

CREATE TABLE [Item]
( 
	[Count]              integer  NULL ,
	[IdItem]             integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdArticle]          integer  NULL ,
	[IdOrder]            integer  NULL 
)
go

CREATE TABLE [Line]
( 
	[Distance]           integer  NULL ,
	[IdLine]             integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdCity]             integer  NULL ,
	[IdC2]               integer  NULL 
)
go

CREATE TABLE [Order]
( 
	[IdOrder]            integer  IDENTITY ( 1,1 )  NOT NULL ,
	[State]              char(100)  NULL ,
	[IdBuyer]            integer  NULL ,
	[ReceivedTime]       datetime  NULL ,
	[SendingTime]        datetime  NULL ,
	[FinalPrice]         numeric(10,3)  NULL ,
	[TransportDays]      integer  NULL 
)
go

CREATE TABLE [Shop]
( 
	[IdShop]             integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdCity]             integer  NULL ,
	[Name]               char(100)  NULL ,
	[Credit]             decimal(10,3)  NULL 
)
go

CREATE TABLE [Transaction]
( 
	[IdTransaction]      integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdOrder]            integer  NULL ,
	[IdBuyer]            integer  NULL ,
	[IdShop]             integer  NULL ,
	[ExecutionTime]      datetime  NULL ,
	[Amount]             decimal(10,3)  NULL 
)
go

ALTER TABLE [Article]
	ADD CONSTRAINT [XPKArticle] PRIMARY KEY  CLUSTERED ([IdArticle] ASC)
go

ALTER TABLE [Buyer]
	ADD CONSTRAINT [XPKBuyer] PRIMARY KEY  CLUSTERED ([IdBuyer] ASC)
go

ALTER TABLE [City]
	ADD CONSTRAINT [XPKCity] PRIMARY KEY  CLUSTERED ([IdCity] ASC)
go

ALTER TABLE [Discount]
	ADD CONSTRAINT [XPKDiscount] PRIMARY KEY  CLUSTERED ([IdDiscount] ASC)
go

ALTER TABLE [Item]
	ADD CONSTRAINT [XPKItem] PRIMARY KEY  CLUSTERED ([IdItem] ASC)
go

ALTER TABLE [Line]
	ADD CONSTRAINT [XPKLine] PRIMARY KEY  CLUSTERED ([IdLine] ASC)
go

ALTER TABLE [Order]
	ADD CONSTRAINT [XPKOrder] PRIMARY KEY  CLUSTERED ([IdOrder] ASC)
go

ALTER TABLE [Shop]
	ADD CONSTRAINT [XPKShop] PRIMARY KEY  CLUSTERED ([IdShop] ASC)
go

ALTER TABLE [Transaction]
	ADD CONSTRAINT [XPKTransaction] PRIMARY KEY  CLUSTERED ([IdTransaction] ASC)
go


ALTER TABLE [Article]
	ADD CONSTRAINT [R_2] FOREIGN KEY ([IdShop]) REFERENCES [Shop]([IdShop])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Buyer]
	ADD CONSTRAINT [R_17] FOREIGN KEY ([IdCity]) REFERENCES [City]([IdCity])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Discount]
	ADD CONSTRAINT [R_3] FOREIGN KEY ([IdShop]) REFERENCES [Shop]([IdShop])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Item]
	ADD CONSTRAINT [R_21] FOREIGN KEY ([IdArticle]) REFERENCES [Article]([IdArticle])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Item]
	ADD CONSTRAINT [R_22] FOREIGN KEY ([IdOrder]) REFERENCES [Order]([IdOrder])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Line]
	ADD CONSTRAINT [R_19] FOREIGN KEY ([IdCity]) REFERENCES [City]([IdCity])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Line]
	ADD CONSTRAINT [R_20] FOREIGN KEY ([IdC2]) REFERENCES [City]([IdCity])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Order]
	ADD CONSTRAINT [R_18] FOREIGN KEY ([IdBuyer]) REFERENCES [Buyer]([IdBuyer])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Shop]
	ADD CONSTRAINT [R_1] FOREIGN KEY ([IdCity]) REFERENCES [City]([IdCity])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Transaction]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([IdOrder]) REFERENCES [Order]([IdOrder])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Transaction]
	ADD CONSTRAINT [R_23] FOREIGN KEY ([IdBuyer]) REFERENCES [Buyer]([IdBuyer])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Transaction]
	ADD CONSTRAINT [R_24] FOREIGN KEY ([IdShop]) REFERENCES [Shop]([IdShop])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


CREATE TRIGGER [dbo].[TR_TRANSFER_MONEY_TO_SHOPS]
   ON  [dbo].[Order]
   AFTER UPDATE
AS 
BEGIN
	declare @cursor cursor
	declare @price decimal(10,3), @total decimal(10,3)
	declare @ida int
	declare @quant int
	declare @suma decimal(10,3),@profit decimal(10,3)
	declare @idc int
	declare @without decimal(10,3),@disc decimal(10,3)
	declare @ido int
	declare @ids int
	declare @transport int
	declare @sentTime Date
	declare @datum Date
	declare @idoo int
	set @suma = 0
	if(update (State))begin
	select @ido = I.IdOrder from
	inserted I, deleted D where I.State = 'arrived' and D.State = 'sent' and I.IdOrder = D.IdOrder
	select @idc = IdBuyer from dbo.[Buyer] where IdBuyer = @ido
	select @total = sum(FinalPrice) from dbo.[Order]
	where State='arrived' and IdBuyer = @idc and DATEDIFF(DAY, ReceivedTime, GETDATE())<30
	select @transport = TransportDays from dbo.[Order] where IdOrder=@ido
	select @sentTime = SendingTime from dbo.[Order] where IdOrder = @ido

	
	set @cursor  = cursor for
	select sum(dbo.[Article].Price*dbo.[Item].Count * (1-cast(Discount.Percentage As decimal(10,3)) / 100)), dbo.[Article].IdShop  from dbo.[Item] join dbo.[Article]
	on dbo.[Item].IdArticle = dbo.[Article].IdArticle join Discount on Discount.IdShop = [Article].IdShop group  by dbo.[Article].IdShop
	
	
	
	open @cursor
	fetch next from @cursor into @suma,@ids

	while @@FETCH_STATUS = 0 begin
	set @datum = DATEADD(DAY, @transport, @sentTime)
	set @suma = @suma * 0.95
	if(@total>10000)begin

	set @suma = @suma * 0.98

	end 
	insert into dbo.[Transaction](IdShop,IdOrder,Amount,ExecutionTime) values(@ids,@ido,@suma,@datum)

	fetch next from @cursor into @suma,@ids
	end
	close @cursor
	deallocate @cursor

	end


END

go

CREATE PROCEDURE SP_FINAL_PRICE
    @OrderId INT
AS
BEGIN
    DECLARE @FinalPrice DECIMAL(10, 3);

	SELECT @FinalPrice = SUM(Article.Price * Item.Count * (1 - cast(Discount.Percentage As decimal(10,3)) / 100))
	FROM Item
	INNER JOIN Article ON Item.IdArticle = Article.IdArticle
	INNER Join Discount on Article.IdShop = Discount.IdShop
	WHERE Item.IdOrder = @OrderId;


    UPDATE [Order]
    SET [Order].FinalPrice = @FinalPrice
    WHERE IdOrder = @OrderId;

    -- Return the final price
    SELECT @FinalPrice AS FinalPrice;
END;