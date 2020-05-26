if __name__ == '__main__':
    destination_practice_folder = r"..\client\src\test\resources\input\practice"
    destination_tournament_folder = r"..\client\src\test\resources\input\tournament"
    numOfFiles = 8
    template_practice = "1\n" \
                        "user{0:05d} pass{0:05d} user{0:05d}@gmail.com ROLE_CLIENT\n" \
                        "2\n" \
                        "user{0:05d} pass{0:05d}\n" \
                        "3\n" \
                        "1\n" \
                        "{1}\n" \
                        "{2}\n" \
                        "{3}\n" \
                        "{4}\n" \
                        "{5}\n" \
                        "{6}\n" \
                        "10"

    template_tournament = "1\n" \
                          "user{0:05d} pass{0:05d} user{0:05d}@gmail.com ROLE_CLIENT\n" \
                          "2\n" \
                          "user{0:05d} pass{0:05d}\n" \
                          "4\n" \
                          "abcd1234\n" \
                          "10"


    def randomTTTplay(i, cell):
        if i % 2 == 0:
            return cell
        else:
            return cell + 3


    for i in range(numOfFiles):
        with open(destination_practice_folder + r'\client_actions_{0:05d}.txt'.format(i), 'w') as out_file:
            out_file.write(template_practice.format(i, randomTTTplay(i, 1), randomTTTplay(i, 2), randomTTTplay(i, 3),
                                                    randomTTTplay(i, 4), randomTTTplay(i, 5), randomTTTplay(i, 6)))

    for i in range(numOfFiles):
        with open(destination_tournament_folder + r'\tournament_player_actions_{0:05d}.txt'.format(i), 'w') as out_file:
            out_file.write(template_tournament.format(i))
