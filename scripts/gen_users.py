import time

if __name__ == '__main__':
    destination_practice_folder = r"..\client\src\test\resources\input\practice"
    destination_tournament_folder = r"..\client\src\test\resources\input\tournament8"
    destination_admin_folder = r"..\client\src\test\resources\input\tournament8"
    numOfFiles = 8
    tournament_size = 8
    tournamentID = 'tournament_{0}'.format(time.time())

    # Pre recorded player actions
    template_practice = "1\n" \
                        "user{0:05d} pass{0:05d} user{0:05d}@gmail.com\n" \
                        "2\n" \
                        "user{0:05d} pass{0:05d}\n" \
                        "3\n" \
                        "1\n" \
                        "6\n" \
                        "{1}\n" \
                        "{2}\n" \
                        "{3}\n" \
                        "10\n" \
                        "10"

    template_admin = "2\n" \
                     "admin admin\n" \
                     "9\n" \
                     "official official official@email.com\n" \
                     "10"

    template_official = "2\n" \
                        "official official\n" \
                        "5\n" \
                        "1 {0} {1}\n" \
                        "10"

    template_tournament = "1\n" \
                          "user{0:05d} pass{0:05d} user{0:05d}@gmail.com\n" \
                          "2\n" \
                          "user{0:05d} pass{0:05d}\n" \
                          "4\n" \
                          "{4}\n" \
                          "6\n" \
                          "{1}\n" \
                          "{2}\n" \
                          "{3}\n" \
                          "4\n" \
                          "{4}\n" \
                          "6\n" \
                          "10"


    def randomTTTplay(i, cell):
        if i % 2 == 0:
            return cell
        else:
            return cell + 3


    with open(destination_admin_folder + r'\admin_actions_1.txt', 'w') as out_file:
        out_file.write(template_admin)

    with open(destination_admin_folder + r'\official_actions_1.txt', 'w') as out_file:
        out_file.write(template_official.format(tournament_size, tournamentID))

    for i in range(numOfFiles):
        with open(destination_practice_folder + r'\client_actions_{0:05d}.txt'.format(i), 'w') as out_file:
            out_file.write(template_practice.format(i, randomTTTplay(i, 1), randomTTTplay(i, 2), randomTTTplay(i, 3)))

    for i in range(numOfFiles):
        with open(destination_tournament_folder + r'\tournament_player_actions_{0:05d}.txt'.format(i), 'w') as out_file:
            out_file.write(template_tournament.format(i, randomTTTplay(i, 1),
                                                      randomTTTplay(i, 2), randomTTTplay(i, 3), tournamentID))
