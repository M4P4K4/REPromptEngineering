import random

class Card:
    def __init__(self, suit, value):
        self.suit = suit
        self.value = value

class Player:
    def __init__(self, name):
        self.name = name
        self.hand = []
        self.discard_pile = []
        self.score = 0

    def draw(self, deck):
        self.hand.append(deck.draw_card())

    def play_card(self, value):
        for card in self.hand:
            if card.value == value:
                self.hand.remove(card)
                return card
        return None

    def take_cards(self, cards):
        self.discard_pile.extend(cards)

class Game:
    def __init__(self):
        self.deck = self.create_deck()
        self.players = [Player("Player 1"), Player("Player 2"), Player("Player 3")]
        self.table = []
        self.turn_order = self.players.copy()
        random.shuffle(self.turn_order)

    def create_deck(self):
        deck = []
        for suit in ["Coins", "Cups", "Swords", "Clubs"]:
            for value in range(1, 11):
                deck.append(Card(suit, value))
        random.shuffle(deck)
        return deck

    def draw_card(self):
        return self.deck.pop()

    def deal_cards(self):
        for _ in range(3):
            for player in self.players:
                player.draw(self)

    def play_round(self):
        self.deal_cards()
        self.table = [self.draw_card() for _ in range(4)]
        while self.deck:
            for player in self.turn_order:
                card = player.play_card(random.choice(range(1, 11)))
                matches = [c for c in self.table if c.value == card.value]
                if matches:
                    player.take_cards(matches)
                    for match in matches:
                        self.table.remove(match)
                else:
                    self.table.append(card)
                if not self.table:
                    player.score += 1
                if not player.hand:
                    self.deal_cards()
        last_player = self.turn_order[-1]
        last_player.take_cards(self.table)
        self.table = []

    def play_game(self):
        while max(player.score for player in self.players) < 8:
            self.play_round()
        winner = max(self.players, key=lambda player: player.score)
        print(f"{winner.name} wins!")

game = Game()
game.play_game()
