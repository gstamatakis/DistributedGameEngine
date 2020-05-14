if __name__ == '__main__':
    destination_practice_folder = r"..\client\src\test\resources\input\practice"
    destination_tournament_folder = r"..\client\src\test\resources\input\tournament"
    numOfFiles = 8
    template_practice = "1\n" \
                        "user{0:05d} pass{0:05d} user{0:05d}@gmail.com ROLE_CLIENT\n" \
                        "2\n" \
                        "user{0:05d} pass{0:05d}\n" \
                        "3\n" \
                        "TIC_TAC_TOE\n" \
                        "10"

    template_tournament = "1\n" \
                          "user{0:05d} pass{0:05d} user{0:05d}@gmail.com ROLE_CLIENT\n" \
                          "2\n" \
                          "user{0:05d} pass{0:05d}\n" \
                          "4\n" \
                          "TIC_TAC_TOE\n" \
                          "10"

    for i in range(numOfFiles):
        with open(destination_practice_folder + r'\client_actions_{0:05d}.txt'.format(i), 'w') as out_file:
            out_file.write(template_practice.format(i))

    for i in range(numOfFiles):
        with open(destination_tournament_folder + r'\client_actions_{0:05d}.txt'.format(i), 'w') as out_file:
            out_file.write(template_tournament.format(i))
