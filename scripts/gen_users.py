if __name__ == '__main__':
    destination_folder = r"..\input\clients"
    numOfFiles = 10
    template = "1\n" \
               "user{0:05d} pass{0:05d} user{0:05d}@gmail.com ROLE_CLIENT\n" \
               "2\n" \
               "user{0:05d} pass{0:05d}\n" \
               "3\n" \
               "TIC_TAC_TOE\n" \
               "10"

    for i in range(numOfFiles):
        with open(destination_folder + r'\client_actions_{0:05d}.txt'.format(i), 'w') as out_file:
            out_file.write(template.format(i))
